(function () {
  const $commentInsertButton = document.getElementById("commentInsertButton");
  // csrfトークンの取得
  const $commentInsertForm = document.getElementById("commentInsertForm");
  const $commentContentInput = document.getElementById("content");

  const commentCsrfToken = $commentInsertForm.children[0].value;
  const commentPostId = document.getElementById("postId").value;

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
        // エラーメッセージノードを削除
        removeErrorMessage($commentContentInput);
        $commentContentInput.value = "";
      })
      .catch((error) => {
        // エラーメッセージノードを削除
        removeErrorMessage($commentContentInput);
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
