import { instance, insertLikeManipulator, deleteLikeManipulator } from "../main";
import 'bootstrap/dist/css/bootstrap.css';
import { Modal } from 'bootstrap';

const $postForm = <HTMLFormElement>document.getElementById("postForm")
const $postContent = <HTMLTextAreaElement>$postForm[1];
const $postButton = <HTMLButtonElement>$postForm[2];
$postButton.setAttribute("disabled", "true");
$postContent.addEventListener("input", (e: Event) => {
	if (!(e.target instanceof HTMLTextAreaElement)) {
		return
	}
	// 全角空白、半角空白、改行のみの投稿に対するバリデーション
	// 1文字以上
	const trimmedTextareaValue = e.target!.value
		.replace(/\r?\n/g, "")
		.replace(/\s+/g, "");

	if (!trimmedTextareaValue) {
		$postButton.setAttribute("disabled", "true");
	} else {
		$postButton.removeAttribute("disabled");
	}
});

const $commentInfoList = <HTMLCollectionOf<HTMLSpanElement>>document.getElementsByClassName("commentInfo");

// コメントモーダル内DOM
const $commentPostModal = <HTMLElement>document.getElementById("commentPostModal");
const $modalBody = <HTMLDivElement>document.getElementById("modalBody");
const $modalForm = <HTMLFormElement>$modalBody.children[0];
const $contentInput = <HTMLTextAreaElement>document.getElementById("postContent");
const $commentInsertButton = <HTMLButtonElement>document.getElementById("commentInsertButton");
const $commentErrorMessage = <HTMLSpanElement>document.getElementById("commentErrorMessage");
const $flashSuccessMessage = <HTMLDivElement>document.getElementById("flashSuccessMessage");
// 非同期通信の際にcsrfTokenが必要
const csrfToken = ($modalForm.children[0] as HTMLInputElement).value;
const commentModal = new Modal($commentPostModal);

// コメント紐付け対象である投稿のpostId
let commentPostId: any = null;
let $commentCountSpan: any = null;

// コメント投稿ボタンのdisalbed判定
$commentInsertButton.setAttribute("disabled", "true");
$contentInput.addEventListener("input", (e) => {
	const trimmedContent = $contentInput.value
		.replace(/\r|\n/g, "")
		.replace(/\s+/g, "");
	if (trimmedContent != "") {
		$commentInsertButton.removeAttribute("disabled");
	} else {
		$commentInsertButton.setAttribute("disabled", "true");
	}
});

// モーダル内コメントの初期化
$commentPostModal.addEventListener("hide.bs.modal", () => {
	commentPostId = null;
	$contentInput.value = "";
	$commentErrorMessage.innerText = "";
});

// クリックした投稿のpostIdを取得
Array.from($commentInfoList).map((commentInfo) =>
	commentInfo.addEventListener("click", (e: Event) => {
		if (!(e.target instanceof HTMLSpanElement)) {
			return
		}
		$flashSuccessMessage.innerText = "";
		commentPostId = e.target.dataset.postId;
		$commentCountSpan = e.target.nextElementSibling;
	})
);

$commentInsertButton.addEventListener("click", (e) => {
	const content = $contentInput.value;
	instance
		.post(
			"/comment",
			{
				postId: commentPostId,
				content: content,
			},
			{
				headers: {
					"Csrf-Token": csrfToken,
				},
			}
		)
		.then((response: any) => {
			$flashSuccessMessage.innerText = "投稿完了しました";
			if ($commentCountSpan.innerText == "") {
				$commentCountSpan.innerText = 1;
			} else {
				$commentCountSpan.innerText =
					parseInt($commentCountSpan.innerText) + 1;
			}

			commentModal.hide();
		})
		.catch((error: any) => {
			if (error.response.status == 401) {
				location.href = "/users/sign-in" + "?=needSignIn";
			}
			let errorMessage = error.response.data.content[0];
			$commentErrorMessage.innerText = errorMessage;
		});
});

// いいねinsert, delete
const $likeInfoList = document.getElementsByClassName("likeInfo");
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
