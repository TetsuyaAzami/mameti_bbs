(function () {
  const $postList = document.getElementById("postList");
  const $modalHiddenInput = document.getElementById("modalHiddenInput");

  console.log($postList);
  $postList.addEventListener("click", (e) => {
    const postId = e.target.dataset.postId;
    $modalHiddenInput.value = postId;
  });
})();
