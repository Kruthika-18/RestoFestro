function validateForm() {
    const name = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const errorMsg = document.getElementById("errorMsg");

    errorMsg.textContent = "";

    if (name === "") {
        errorMsg.textContent = "Name is required.";
        return false;
    }

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(email)) {
        errorMsg.textContent = "Enter a valid email address.";
        return false;
    }

    const phonePattern = /^[0-9]{10}$/;
    if (!phonePattern.test(phone)) {
        errorMsg.textContent = "Enter a valid 10-digit phone number.";
        return false;
    }

    if (password.length < 6) {
        errorMsg.textContent = "Password must be at least 6 characters.";
        return false;
    }

    if (password !== confirmPassword) {
        errorMsg.textContent = "Passwords do not match.";
        return false;
    }

    return true;
}