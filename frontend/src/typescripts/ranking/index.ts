import { deleteLikeManipulator, insertLikeManipulator } from "../main";

// 部門activeタブ判定
const url: URL = new URL(location.href);
const search: string = url.search;
const decodedsearch: string = decodeURI(search);

// クエリパラメータdepartmentのvalueを抽出
// (http://localhost/ranking?department=フロントエンド&sortBy=like) → フロントエンド
const queryDepartmentIndex = decodedsearch.indexOf("department");
const ampersandIndex = decodedsearch.indexOf("&");
const selectedDepartment = decodedsearch.substring(
	queryDepartmentIndex + "department".length + 1,
	ampersandIndex
);

const navLinks = document.getElementsByClassName("nav-link");
// タブ初期化処理
Array.from(navLinks).forEach((navLink) => navLink.classList.remove("active"));
if (selectedDepartment == "フロントエンド") {
	navLinks[1].classList.add("active");
} else if (selectedDepartment == "バックエンド") {
	navLinks[2].classList.add("active");
} else if (selectedDepartment == "クラウド") {
	navLinks[3].classList.add("active");
} else {
	navLinks[0].classList.add("active");
}

// いいねinsert, delete
const csrfToken = (document.getElementById("signOutForm")!.children[0] as HTMLInputElement).value
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
		$heartCountSpan.innerText = response.data;
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
