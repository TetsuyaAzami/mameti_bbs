(function () {
  const fileSizeLimit = 1024 * 1024 * 2;
  const $profileImgInput = document.getElementById("profileImgInput");
  const $profileImgError = document.getElementById("profileImgError");
  const $preview = document.getElementById("preview");
  const $profileImg = $preview.children.item(0);
  const $updateButton = document.getElementById("updateButton");

  // 初期化処理
  const initFileHandler = () => {
    $profileImgError.innerText = "";
    $updateButton.disabled = false;
  };

  //ファイルサイズを2MBに制限。
  const fileSizeCheck = (file) => {
    const fileSize = file.size;
    if (fileSize > fileSizeLimit) {
      $profileImgError.innerText =
        "ファイルサイズが大きすぎます。2MB以内にしてください";
      $updateButton.disabled = true;
      return false;
    } else {
      return true;
    }
  };

  //プレビューの表示
  const previewFile = (file) => {
    const reader = new FileReader();

    reader.onload = (e) => {
      const imageUrl = e.target.result;
      $profileImg.src = imageUrl;
    };

    reader.readAsDataURL(file);
  };

  //各種ファイル処理
  const fileHandler = () => {
    initFileHandler();
    const files = $profileImgInput.files;
    Array.from(files).map((file) => {
      //ファイルサイズが適正なら画像のプレビューを表示
      if (fileSizeCheck(file) == true) {
        previewFile(file);
      }
    });
  };

  $profileImgInput.addEventListener("change", () => {
    fileHandler();
  });
})();
