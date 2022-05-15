(function () {
  const $postForm = document.forms.postForm;
  const $postContent = $postForm[1];
  const $postButton = $postForm[2];
  // 全角空白、半角空白、改行のみの投稿に対するバリデーション
  // 1文字以上
  $postButton.setAttribute("disabled", true);
  $postContent.addEventListener("input", (e) => {
    const trimmedTextareaValue = e.target.value
      .replace(/\r?\n/g, "")
      .replace(/\s+/g, "");

    if (!trimmedTextareaValue) {
      $postButton.setAttribute("disabled", true);
    } else {
      $postButton.removeAttribute("disabled");
    }
  });

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
  const csrfToken = $modalForm.children[0].value;
  const commentModal = new bootstrap.Modal($commentPostModal);

  // コメント紐付け対象である投稿のpostId
  let commentPostId = null;
  let $commentCountSpan = null;

  // コメント投稿ボタンのdisalbed判定
  $commentInsertButton.setAttribute("disabled", true);
  $contentInput.addEventListener("input", (e) => {
    const trimmedContent = $contentInput.value
      .replace(/\r|\n/g, "")
      .replace(/\s+/g, "");
    if (trimmedContent != "") {
      $commentInsertButton.removeAttribute("disabled");
    } else {
      $commentInsertButton.setAttribute("disabled", true);
    }
  });

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
            "Csrf-Token": csrfToken,
          },
        }
      )
      .then((response) => {
        $flashSuccessMessage.innerText = "投稿完了しました";
        if ($commentCountSpan.innerText == "") {
          $commentCountSpan.innerText = 1;
        } else {
          $commentCountSpan.innerText =
            parseInt($commentCountSpan.innerText) + 1;
        }

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
