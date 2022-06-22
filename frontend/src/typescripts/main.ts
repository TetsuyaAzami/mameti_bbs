import axios from 'axios';

// axiosインスタンス
export const instance = axios.create({
	baseURL: "/",
	timeout: 1000,
	headers: { "Content-Type": "application/json" },
});

//サインインページにリダイレクト(認証)
export const needSignInFunction = (error: any) => {
	if (error.response.status == 401) {
		location.href = "/users/sign-in" + "?=needSignIn";
	}
};

//like操作の抽象メソッド
export const likeManipulator = (url: any, likePostId: any, csrfToken: any, successFunction: any) => {
	instance
		.post(
			url,
			{
				postId: likePostId,
			},
			{
				headers: {
					"Csrf-Token": csrfToken,
				},
			}
		)
		.then((response: any) => successFunction(response))
		.catch((error: any) => {
			needSignInFunction(error);
		});
};

//likeのinsert操作抽象メソッド
export const insertLikeManipulator = (likePostId: any, csrfToken: any, insertSuccessFunction: any) =>
	likeManipulator("/like/insert", likePostId, csrfToken, insertSuccessFunction);

//likeのdelete操作抽象メソッド
export const deleteLikeManipulator = (likePostId: any, csrfToken: any, deleteSuccessFunction: any) =>
	likeManipulator("/like/delete", likePostId, csrfToken, deleteSuccessFunction);

//エラーメッセージを表示するノードを作成
export const createErrorMesssage = (errorMessage: any) => {
	const errorMessageNode = document.createElement("div");
	errorMessageNode.innerText = errorMessage;
	errorMessageNode.style.color = "red";
	errorMessageNode.style.paddingTop = "16px";
	errorMessageNode.style.paddingLeft = "16px";
	return errorMessageNode;
};

// エラーメッセージをDOMに追加
export const displayErrorMessage = (errorMessageNode: any, NodeThatHasError: any) => {
	NodeThatHasError.parentNode.insertBefore(errorMessageNode, NodeThatHasError);
};

// エラーメッセージノードを削除
export const removeErrorMessage = (errorMessageNode: any) => {
	if (errorMessageNode.previousElementSibling !== null) {
		errorMessageNode.previousElementSibling.remove();
	}
};

export const formatDateUtil = (commentDate: any) => {
	const month = commentDate.substring(5, 7);
	const date = commentDate.substring(8, 10);
	const hour = commentDate.substring(11, 13);
	const minute = commentDate.substring(14, 16);
	return `${month}/${date} ${hour}:${minute}`;
};

// コメントヘッダーの作成
export const createCommentCardHeader = (commentData: any) => {
	const sectionHeader = document.createElement("section");
	sectionHeader.classList.add("card-header");
	sectionHeader.classList.add("bg-white");

	//プロフィール画像img
	const profileImg = document.createElement("img");
	profileImg.src = `https://static.mameti-bbs.com/${commentData.userWhoCommented.profileImg}`;
	profileImg.classList.add("profile-img-sm");
	profileImg.setAttribute("alt", "プロフィール画像");

	//ユーザ名span
	const spanUserName = document.createElement("span");
	spanUserName.classList.add("fs-5");
	spanUserName.classList.add("fw-bold");
	spanUserName.innerText = commentData.userWhoCommented.name;

	//時刻span
	const spanTime = document.createElement("span");
	spanTime.classList.add("ps-3");
	spanTime.classList.add("text-secondary");
	spanTime.innerText = formatDateUtil(commentData.commentedAt);

	//spanをsectionHeaderに追加
	sectionHeader.append(profileImg);
	sectionHeader.append(spanUserName);
	sectionHeader.append(spanTime);
	return sectionHeader;
};

//コメント いいね アイコンDivを作成
export const createIconsDiv = () => {
	const iconsDiv = document.createElement("div");
	iconsDiv.classList.add("px-3");
	iconsDiv.classList.add("w-25");
	iconsDiv.classList.add("d-flex");
	iconsDiv.classList.add("justify-content-between");

	//コメント
	const comment = document.createElement("i");
	comment.title = "コメントする";
	comment.classList.add("far");
	comment.classList.add("fa-comment");

	//いいね
	const like = document.createElement("i");
	like.title = "いいね！";
	like.classList.add("far");
	like.classList.add("fa-heart");

	//divにコメントといいねを追加
	iconsDiv.append(comment);
	iconsDiv.append(like);
	return iconsDiv;
};

export const createCommentCardBody = (commentData: any) => {
	// カード内ボディ
	const sectionBody = document.createElement("section");
	sectionBody.classList.add("card-body");

	// コメントコンテンツ
	const contentDiv = document.createElement("div");
	contentDiv.classList.add("card-text");
	contentDiv.classList.add("p-3");
	contentDiv.innerText = commentData.content;

	const iconsDiv = createIconsDiv();

	//sectionBodyにコンテンツとアイコンDivを追加
	sectionBody.append(contentDiv);
	sectionBody.append(iconsDiv);

	return sectionBody;
};

//作成されたコメントをコメントリストに追加
export const appendCommentArticle = (parentNode: any, commentData: any) => {
	const article = document.createElement("article");
	article.classList.add("border");
	article.classList.add("rounded");

	const commentCardHeader = createCommentCardHeader(commentData);
	const commentCardBody = createCommentCardBody(commentData);

	article.appendChild(commentCardHeader);
	article.appendChild(commentCardBody);

	parentNode.prepend(article);
};
