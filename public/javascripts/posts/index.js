(function () {
  const $postList = document.getElementById("postList");
  // コメントモーダル内DOM
  const $commentPostModal = document.getElementById("commentPostModal");
  const $modalBody = document.getElementById("modalBody");
  const $modalForm = $modalBody.children[0];
  const $contentInput = document.getElementById("content");
  const $commentInsertButton = document.getElementById("commentInsertButton");
  const $commentErrorMessage = document.getElementById("commentErrorMessage");
  const $flashSuccessMessage = document.getElementById("flashSuccessMessage");

  const commentModal = new bootstrap.Modal($commentPostModal);

  // コメント紐付け対象である投稿のpostId
  let commentPostId = null;
  // 非同期通信の際にcsrfTokenが必要
  const commentCsrfToken = $modalForm.children[0].value;

  // モーダル内コメントの初期化
  $commentPostModal.addEventListener("hide.bs.modal", () => {
    commentPostId = null;
    $contentInput.value = "";
    $commentErrorMessage.innerText = "";
  });

  // クリックした投稿のpostIdを取得
  $postList.addEventListener("click", (e) => {
    $flashSuccessMessage.innerText = "";
    commentPostId = e.target.dataset.postId;
  });

  $commentInsertButton.addEventListener("click", (e) => {
    const content = $contentInput.value;
    axios
      .post(
        "/comment",
        {
          postId: commentPostId,
          content: content,
        },
        {
          headers: {
            "Content-Type": "application/json",
            "Csrf-Token": commentCsrfToken,
          },
        }
      )
      .then((response) => {
        $flashSuccessMessage.innerText = response.data;
        commentModal.hide();
      })
      .catch((error) => {
        let errorMessage = error.response.data.content[0];
        $commentErrorMessage.innerText = errorMessage;
      });
  });
})();
