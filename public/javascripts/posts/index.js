(function () {
  const $commentInfoList = document.getElementsByClassName("commentInfo");
  // コメントモーダル内DOM
  const $commentPostModal = document.getElementById("commentPostModal");
  const $modalBody = document.getElementById("modalBody");
  const $modalForm = $modalBody.children[0];
  const $contentInput = document.getElementById("content");
  const $commentInsertButton = document.getElementById("commentInsertButton");
  const $commentErrorMessage = document.getElementById("commentErrorMessage");
  const $flashSuccessMessage = document.getElementById("flashSuccessMessage");
  // 非同期通信の際にcsrfTokenが必要
  const commentCsrfToken = $modalForm.children[0].value;

  const commentModal = new bootstrap.Modal($commentPostModal);

  // コメント紐付け対象である投稿のpostId
  let commentPostId = null;
  let $commentCountSpan = null;

  // モーダル内コメントの初期化
  $commentPostModal.addEventListener("hide.bs.modal", () => {
    commentPostId = null;
    $contentInput.value = "";
    $commentErrorMessage.innerText = "";
  });

  // クリックした投稿のpostIdを取得
  Array.from($commentInfoList).map((commentInfo) =>
    commentInfo.addEventListener("click", (e) => {
      $flashSuccessMessage.innerText = "";
      commentPostId = e.target.dataset.postId;
      $commentCountSpan = e.target.nextElementSibling;
      console.log($commentCountSpan);
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
            "Content-Type": "application/json",
            "Csrf-Token": commentCsrfToken,
          },
        }
      )
      .then((response) => {
        $flashSuccessMessage.innerText = "投稿完了しました";
        $commentCountSpan.innerText = parseInt($commentCountSpan.innerText) + 1;
        commentModal.hide();
      })
      .catch((error) => {
        if (error.response.status == 401) {
          location.href = "/users/sign-in" + "?=needSignIn";
        }
        let errorMessage = error.response.data.content[0];
        $commentErrorMessage.innerText = errorMessage;
      });
  });
})();
