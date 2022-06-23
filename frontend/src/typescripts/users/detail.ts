import { deleteLikeManipulator, formatDateUtil, insertLikeManipulator, instance } from "../main";

const $deleteForms = <HTMLCollectionOf<HTMLFormElement>>document.getElementsByClassName("deleteForm");

// 投稿の削除
[...$deleteForms].map((deleteForm) => {
	const deleteButton = <HTMLFormElement>deleteForm.children[1];
	deleteButton.addEventListener("click", (e) => {
		if (confirm("本当に削除してよろしいですか？")) {
			deleteForm.submit();
		}
	});
});

/**
 * いいね insert, delete
 */
const $likeInfoList = document.getElementsByClassName("likeInfo");
const csrfToken = ($deleteForms[0].children[0] as HTMLInputElement).value;
Array.from($likeInfoList).map((likeInfo) => {
	const $heart = likeInfo.children[0];
	const $heartCountSpan = <HTMLSpanElement>likeInfo.children[1];

	//いいねinsert 成功パターン
	const insertSuccessFunction = (response: any) => {
		$heartCountSpan.innerText = response.data;
		$heart.classList.remove("like-btn");
		$heart.classList.add("unlike-btn");
	};

	//いいねdelete 成功パターン
	const deleteSuccessFunction = (response: any) => {
		$heartCountSpan.innerText = response.data;
		$heart.classList.remove("unlike-btn");
		$heart.classList.add("like-btn");
	};

	$heart.addEventListener("click", (e) => {
		if (!(e.target instanceof HTMLElement)) {
			return
		}
		const likePostId = e.target.dataset.postId;
		const isInsertButton = e.target.classList.contains("like-btn");
		if (isInsertButton) {
			insertLikeManipulator(likePostId, csrfToken, insertSuccessFunction);
		} else {
			deleteLikeManipulator(likePostId, csrfToken, deleteSuccessFunction);
		}
	});
});

/**
 * コメント非同期編集
 */
const $editFormGenerateButtonList = document.getElementsByClassName(
	"editFormGenerateButton"
);
const $cardTextareaList = <HTMLCollectionOf<HTMLTextAreaElement>>document.getElementsByClassName("card-textarea");

//編集ボタンにイベントを追加(対象のtextareaにfocusするまで)
Array.from($editFormGenerateButtonList).map(($editFormGenerateButton) =>
	$editFormGenerateButton.addEventListener("click", (e) => {
		if (!(e.target instanceof HTMLElement)) {
			return
		}
		const postId = e.target.dataset.postId;
		// クリックされた投稿のbody部分を取得
		const $cardBodySection = extractCardBodySection(postId);

		// 投稿編集ブロック取得
		const $contentEditDiv = $cardBodySection!.children[1];
		// 既存コンテンツ取得
		const $existingPostContentDiv = <HTMLElement>$cardBodySection!.children[0];
		const $editTextarea = $contentEditDiv.children[0];

		displayTextarea($contentEditDiv, $existingPostContentDiv);

		// textarea内コンテンツの最後にfocusを当てる
		focusTextarea(
			$editTextarea,
			extractCursorPoint($existingPostContentDiv.innerText)
		);
	})
);

// textareaにfocusされた時とfocusが外れたときのイベントを追加
Array.from($cardTextareaList).map(($cardTextarea) => {
	const $contentEditDiv = <HTMLElement>$cardTextarea.parentNode!;
	const $existingPostContentDiv = <HTMLElement>$contentEditDiv.previousElementSibling;
	const $contentFooter =
		<HTMLElement>($contentEditDiv.parentNode as HTMLElement).nextElementSibling;
	const $cardEditButton =
		$cardTextarea.nextElementSibling!.firstElementChild!.firstElementChild;
	const postId = parseInt(($contentEditDiv.parentNode! as HTMLElement).dataset.postId!);

	$cardTextarea.addEventListener(
		"focus",
		() => {
			$contentFooter!.classList.add("display-none");
			// 編集確定操作
			$cardEditButton!.addEventListener("click", (e) => {
				e.stopPropagation();
				// id = "jsPostErrorMsgDiv"であるエラーメッセージを消す
				removeErrorMsgDiv("jsPostErrorMsgDiv");
				const content = $cardTextarea.value;
				// axios 投稿更新処理
				instance
					.post(
						`/posts/update`,
						{
							postId: postId,
							content: content,
							postedAt: Date.now(),
						},
						{
							headers: {
								"Csrf-Token": csrfToken,
							},
						}
					)
					.then((response) => {
						// 既存コンテンツDivに更新した投稿を反映して表示
						const updatedContent = response.data;
						const $postCardHeader =
							($contentEditDiv.parentNode as HTMLElement).previousElementSibling;
						const $postedAtSpan =
							<HTMLElement>$postCardHeader!.children[0].lastElementChild;
						$existingPostContentDiv.innerText = updatedContent.content;
						$postedAtSpan.innerText = formatDateUtil(updatedContent.postedAt);

						displayExistingContent($contentEditDiv, $existingPostContentDiv);
						$contentFooter.classList.remove("display-none");
					})
					.catch((error) => {
						const errors = error.response.data;
						const contentObj = errors.errors["obj.content"][0];
						const errorMsg = contentObj.msg[0];
						const $cardBody = $contentEditDiv.parentNode;
						prependErrorMsg($cardBody, errorMsg);
						focusTextarea($cardTextarea, extractCursorPoint(content));
					});
			});
		},
		{ once: true }
	);
	$cardTextarea.addEventListener("blur", (e) => {
		const blurTarget = (e as MouseEvent).relatedTarget;
		if (blurTarget == null || (blurTarget as HTMLButtonElement).name != "editDecideButton") {
			if (confirm("編集内容を破棄してもよろしいでしょうか？")) {
				// id = "jsPostErrorMsgDiv"であるエラーメッセージを消す
				removeErrorMsgDiv("jsPostErrorMsgDiv");
				// 既存コンテンツに内容を修正
				const existingContent = $existingPostContentDiv.innerText;
				$cardTextarea.value = existingContent;
				displayExistingContent($contentEditDiv, $existingPostContentDiv);
				$contentFooter.classList.remove("display-none");
			} else {
				// キャンセルをクリックした際にblurが発動しないように少し待つ
				setTimeout(() => {
					// 編集中のtextareaに再度focus
					focusTextarea(
						$cardTextarea,
						extractCursorPoint($cardTextarea.value)
					);
				}, 10);
			}
		}
	});
});

// クリックされた投稿のcard-bodyを返す
const extractCardBodySection = (postId: any): HTMLElement | null => {
	const $cardBodySectionList = <NodeListOf<HTMLElement>>document.querySelectorAll("section.card-body");
	let $taragetCardBodySection: HTMLElement | null = null;
	Array.from($cardBodySectionList).map(($cardBodySection) => {
		if ($cardBodySection.dataset.postId == postId) {
			$taragetCardBodySection = $cardBodySection;
		}
	});
	return $taragetCardBodySection;
};

// 編集textareaを表示して既存コンテンツを非表示にする
const displayTextarea = ($contentEditDiv: any, $existingPostContentDiv: any) => {
	$existingPostContentDiv.classList.add("display-none");
	$contentEditDiv.classList.remove("display-none");
};

// 既存コンテンツを表示して編集textareaを非表示にする
const displayExistingContent = ($contentEditDiv: any, $existingPostContentDiv: any) => {
	$contentEditDiv.classList.add("display-none");
	$existingPostContentDiv.classList.remove("display-none");
};

// focus時のカーソル位置を決める(
const extractCursorPoint = (content: any) => {
	const contentLength = content.length;
	return contentLength;
};

//forcusTargetにfocus
const focusTextarea = ($forcusTarget: any, cursorPosition: any) => {
	$forcusTarget.focus();
	$forcusTarget.setSelectionRange(cursorPosition, cursorPosition);
};

// errorMsgをtargetNodeの先頭子要素に追加
const prependErrorMsg = ($targetNode: any, errorMsg: any) => {
	const $errorMsgDiv = produceErrorMsgDiv(errorMsg);
	$targetNode.prepend($errorMsgDiv);
};

// エラーメッセージ表示Div作成
const produceErrorMsgDiv = (errorMsg: any) => {
	const $errorMsgDiv = document.createElement("div");
	$errorMsgDiv.innerText = errorMsg;
	$errorMsgDiv.style.padding = "16px";
	$errorMsgDiv.classList.add("error-message");
	$errorMsgDiv.id = "jsPostErrorMsgDiv";
	return $errorMsgDiv;
};

// エラーメッセージを消す
const removeErrorMsgDiv = (id: any) => {
	const $ErrorMsgDiv = document.getElementById(id);
	if ($ErrorMsgDiv != null) {
		$ErrorMsgDiv.remove();
	}
};
