function validateLoginForm() {
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const errorMsg = document.getElementById("errorMsg");

    errorMsg.textContent = "";

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(email)) {
        errorMsg.textContent = "Enter a valid email address.";
        return false;
    }

    if (password === "") {
        errorMsg.textContent = "Password is required.";
        return false;
    }

    return true;
}