console.log("login.js loaded");

document.getElementById('loginForm').addEventListener('submit', handleLogin);

async function handleLogin(event) {
    event.preventDefault();
    console.log("handleLogin called");
    const form = event.target;
    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());

    try {
        const response = await fetch('/alunos/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data),
            credentials: 'include' // Importante para incluir cookies de sessão
        });

        if (response.ok) {
            console.log("Login successful, redirecting to /home");
            window.location.href = '/home';
        } else {
            const errorResponse = await response.json();
            console.log("Login failed:", errorResponse);
            alert(errorResponse.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Ocorreu um erro ao tentar fazer login. Por favor, tente novamente.');
    }
}