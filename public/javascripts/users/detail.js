(function () {
  const $deleteForms = document.getElementsByClassName("deleteForm");

  // 投稿の削除
  [...$deleteForms].map((deleteForm) => {
    const deleteButton = deleteForm.children.deleteButton;
    deleteButton.addEventListener("click", (e) => {
      if (confirm("本当に削除してよろしいですか？")) {
        deleteForm.submit();
      }
    });
  });

  // いいね insert, delete
  const $likeInfoList = document.getElementsByClassName("likeInfo");
  const csrfToken = $deleteForms[0].children[0].value;
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
