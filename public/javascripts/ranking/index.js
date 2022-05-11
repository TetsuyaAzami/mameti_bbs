(function () {
  // 部門activeタブ判定
  const url = new URL(location.href);
  const search = url.search;
  const decodedsearch = decodeURI(search);
  // クエリパラメータdepartmentのvalueを抽出
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
  const csrfToken = document.forms.signOutForm.children[0].value;
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
