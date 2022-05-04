(function () {
  const fileSizeLimit = 1024 * 1024 * 2;
  const $profileImgInput = document.getElementById("profileImgInput");
  const $profileImgError = document.getElementById("profileImgError");
  const $updateButton = document.getElementById("updateButton");

  //ファイルサイズを2MBに制限。
  const fileSizeValidation = () => {
    // 初期化処理
    $profileImgError.innerText = "";
    $updateButton.disabled = false;

    const files = $profileImgInput.files;
    Array.from(files).map((file) => {
      const fileSize = file.size;
      if (fileSize > fileSizeLimit) {
        $profileImgError.innerText =
          "ファイルサイズが大きすぎます。2MB以内にしてください";
        $updateButton.disabled = true;
      }
    });
  };

  $profileImgInput.addEventListener("change", () => {
    fileSizeValidation();
  });
})();
