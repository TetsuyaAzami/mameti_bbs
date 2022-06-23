import { appendCommentArticle, createErrorMesssage, deleteLikeManipulator, displayErrorMessage, insertLikeManipulator, instance, removeErrorMessage } from "../main";

const $commentInsertButton = <HTMLButtonElement>document.getElementById("commentInsertButton")!;
const $commentInsertForm = <HTMLFormElement>document.getElementById("commentInsertForm")!;
const $commentContentInput = <HTMLTextAreaElement>document.getElementById("content")!;
const $commentList = document.getElementById("commentList")!;
const $commentCount = document.getElementById("commentCount")!;
let commentCount: number = parseInt($commentCount.innerText);

const csrfToken = ($commentInsertForm.children[0] as HTMLInputElement).value;
const commentPostId = (document.getElementById("postId") as HTMLInputElement).value;

// 全角空白、半角空白、改行のみのコメントに対するバリデーション
$commentInsertButton.setAttribute("disabled", "true");
$commentContentInput.addEventListener("input", (e: Event) => {
	if (!(e.target instanceof HTMLTextAreaElement)) {
		return
	}
	const trimmedTextareaValue = e.target.value
		.replace(/\r?\n/g, "")
		.replace(/\s+/g, "");
	if (!trimmedTextareaValue) {
		$commentInsertButton.setAttribute("disabled", "true");
	} else {
		$commentInsertButton.removeAttribute("disabled");
	}
});

// コメント投稿処理
$commentInsertButton.addEventListener("click", (e) => {
	let content = $commentContentInput.value;
	instance
		.post(
			"/comment",
			{
				postId: commentPostId,
				content: content,
			},
			{
				headers: {
					"Content-Type": "application/json",
					"Csrf-Token": csrfToken,
				},
			}
		)
		.then((response) => {
			appendCommentArticle($commentList, response.data);
			// エラーメッセージノードを削除
			removeErrorMessage($commentContentInput);
			$commentContentInput.value = "";

			if ($commentCount.innerText == "") {
				commentCount = 1;
				$commentCount.innerText = commentCount.toFixed();
			} else {
				commentCount += 1;
				$commentCount.innerText = commentCount.toFixed();
			}
		})
		.catch((error) => {
			if (error.response.status == 401) {
				location.href = "/users/sign-in" + "?=needSignIn";
			}
			// エラーメッセージノードを削除
			removeErrorMessage($commentContentInput);
			//エラ-メッセージを取得 あとで実装
			let errorMessage = error.response.data.content[0];
			//エラーメッセージを表示
			displayErrorMessage(
				// エラーメッセージを持つDOMを作成
				createErrorMesssage(errorMessage),
				// このノードの前にエラーDOMを追加
				$commentContentInput
			);
		});
});

// いいねinsert, delete
const $likeInfoList = document.getElementsByClassName("likeInfo");
Array.from($likeInfoList).map((likeInfo) => {
	const $heart = <HTMLElement>likeInfo.children[0];
	const $heartCountSpan = <HTMLSpanElement>likeInfo.children[1];

	//いいねinsert 成功パターン
	const insertSuccessFunction = (response: any) => {
		$heartCountSpan.innerText = response.data;
		$heart.classList.remove("like-btn");
		$heart.classList.add("unlike-btn");
	};
	//いいねdelete 成功パターン
	const deleteSuccessFunction = (response: any) => {
		if (response.data == 0) {
			$heartCountSpan.innerText = "";
		} else {
			$heartCountSpan.innerText = response.data;
		}
		$heart.classList.remove("unlike-btn");
		$heart.classList.add("like-btn");
	};

	$heart.addEventListener("click", (e: Event) => {
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
