export { }
const $errorNeedSignIn = <HTMLElement>document.getElementById("errorNeedSignIn");
if (location.search == "?=needSignIn") {
	$errorNeedSignIn.classList.remove("display-none");
}
