(function () {
  const $deleteForms = document.getElementsByClassName("deleteForm");

  [...$deleteForms].map((deleteForm) => {
    const deleteButton = deleteForm.children.deleteButton;
    deleteButton.addEventListener("click", (e) => {
      if (confirm("本当に削除してよろしいですか？")) {
        console.log(deleteForm.submit());
      }
    });
  });
})();
