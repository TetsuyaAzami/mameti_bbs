(function () {
  const $commentInsertButton = document.getElementById("commentInsertButton");
  const $commentInsertForm = document.getElementById("commentInsertForm");
  const $commentContentInput = document.getElementById("content");
  const $commentList = document.getElementById("commentList");
  const $commentCount = document.getElementById("commentCount");

  const commentCsrfToken = $commentInsertForm.children[0].value;
  const commentPostId = document.getElementById("postId").value;

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
            "Csrf-Token": commentCsrfToken,
          },
        }
      )
      .then((response) => {
        appendCommentArticle($commentList, response.data);
        // エラーメッセージノードを削除
        removeErrorMessage($commentContentInput);
        $commentContentInput.value = "";
        $commentCount.innerText = parseInt($commentCount.innerText) + 1;
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
})();
