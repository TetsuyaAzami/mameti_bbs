export { }
const fileSizeLimit = 1024 * 1024 * 2;
const $profileImgInput = <HTMLElement>document.getElementById("profileImgInput");
const $profileImgError = <HTMLElement>document.getElementById("profileImgError");
const $preview = <HTMLElement>document.getElementById("preview");
const $profileImg = <HTMLImageElement>$preview.children.item(0);
const $updateButton = <HTMLButtonElement>document.getElementById("updateButton");

// 初期化処理
const initFileHandler = () => {
	$profileImgError.innerText = "";
	$updateButton.disabled = false;
};

//ファイルサイズを2MBに制限。
const fileSizeCheck = (file: any) => {
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
const previewFile = (file: any) => {
	const reader = new FileReader();

	reader.onload = (e: Event) => {
		const imageUrl = (e.target as any).result;
		$profileImg.src = imageUrl;
	};

	reader.readAsDataURL(file);
};

//各種ファイル処理
const fileHandler = () => {
	initFileHandler();
	const files = ($profileImgInput as any).files;
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
