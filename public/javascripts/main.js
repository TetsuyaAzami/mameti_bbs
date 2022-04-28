// axiosインスタンス
const instance = axios.create({
  baseURL: "/",
  timeout: 1000,
  headers: { "Content-Type": "application/json" },
});

//エラーメッセージを表示するノードを作成
const createErrorMesssage = (errorMessage) => {
  const errorMessageNode = document.createElement("div");
  errorMessageNode.innerText = errorMessage;
  errorMessageNode.style.color = "red";
  errorMessageNode.style.paddingTop = "16px";
  errorMessageNode.style.paddingLeft = "16px";
  return errorMessageNode;
};

// エラーメッセージをDOMに追加
const displayErrorMessage = (errorMessageNode, NodeThatHasError) => {
  NodeThatHasError.parentNode.insertBefore(errorMessageNode, NodeThatHasError);
};

// エラーメッセージノードを削除
const removeErrorMessage = (errorMessageNode) => {
  if (errorMessageNode.previousElementSibling !== null) {
    errorMessageNode.previousElementSibling.remove();
  }
};

const formatDateUtil = (commentDate) => {
  const month = commentDate.substring(5, 7);
  const date = commentDate.substring(8, 10);
  const hour = commentDate.substring(11, 13);
  const minute = commentDate.substring(14, 16);
  return `${month}/${date} ${hour}:${minute}`;
};

// コメントヘッダーの作成
const createCommentCardHeader = (commentData) => {
  const sectionHeader = document.createElement("section");
  sectionHeader.classList.add("card-header");
  sectionHeader.classList.add("bg-white");

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
  sectionHeader.append(spanUserName);
  sectionHeader.append(spanTime);
  return sectionHeader;
};

//コメント いいね アイコンDivを作成
const createIconsDiv = () => {
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

const createCommentCardBody = (commentData) => {
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
const appendCommentArticle = (parentNode, commentData) => {
  const article = document.createElement("article");
  article.classList.add("border");
  article.classList.add("rounded");

  const commentCardHeader = createCommentCardHeader(commentData);
  const commentCardBody = createCommentCardBody(commentData);

  article.appendChild(commentCardHeader);
  article.appendChild(commentCardBody);

  parentNode.prepend(article);
};
