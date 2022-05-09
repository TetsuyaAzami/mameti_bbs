(function () {
  const $commentInsertButton = document.getElementById("commentInsertButton");
  const $commentInsertForm = document.getElementById("commentInsertForm");
  const $commentContentInput = document.getElementById("content");
  const $commentList = document.getElementById("commentList");
  const $commentCount = document.getElementById("commentCount");

  const csrfToken = $commentInsertForm.children[0].value;
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
            "Csrf-Token": csrfToken,
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

  // いいねinsert, delete
  const $likeInfoList = document.getElementsByClassName("likeInfo");
  Array.from($likeInfoList).map((likeInfo) => {
    const $heart = likeInfo.children[0];
    const $heartCountSpan = likeInfo.children[1];

    //いいねinsert 成功パターン
    const insertSuccessFunction = (response) => {
      $heartCountSpan.innerText = response.data;
      $heart.classList.remove("like-btn");
      $heart.classList.add("unlike-btn");
    };
    //いいねdelete 成功パターン
    const deleteSuccessFunction = (response) => {
      $heartCountSpan.innerText = response.data;
      $heart.classList.remove("unlike-btn");
      $heart.classList.add("like-btn");
    };

    $heart.addEventListener("click", (e) => {
      const likePostId = e.target.dataset.postId;
      const isInsertButton = e.target.classList.contains("like-btn");
      if (isInsertButton) {
        insertLikeManipulator(likePostId, csrfToken, insertSuccessFunction);
      } else {
        deleteLikeManipulator(likePostId, csrfToken, deleteSuccessFunction);
      }
    });
  });
})();
