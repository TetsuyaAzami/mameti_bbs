(function () {
  const $deletebutton = document.getElementById("deleteButton");
  const $deleteForm = document.getElementById("deleteForm");
  $deletebutton.addEventListener("click", () => {
    if (confirm("本当に削除してよろしいですか？")) {
      $deleteForm.submit();
    }
  });
})();
