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
