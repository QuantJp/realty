// 이메일 인증 관련 JavaScript 코드

document.addEventListener('DOMContentLoaded', function() {
    // 인증 코드 전송 버튼 클릭 이벤트 핸들러
    document.getElementById("sendVerificationCodeBtn").addEventListener("click", function() {
        const emailInput = document.getElementById("email");
        const email = emailInput.value;

        // 이메일 필드에 대한 HTML5 유효성 검사
        if (!emailInput.checkValidity()) {
            emailInput.reportValidity();
            return;
        }

        if (email) {
            fetch("/send-verification-code", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                body: `email=${encodeURIComponent(email)}`
            })
            .then(response => {
                if (response.ok) {
                    alert("인증 코드가 발송되었습니다.");
                } else {
                    // 서버 응답에서 오류 메시지를 파싱하여 표시
                    response.text().then(errorMessage => {
                        alert("인증 코드 발송에 실패했습니다: " + errorMessage);
                    }).catch(() => {
                        alert("인증 코드 발송에 실패했습니다.");
                    });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("네트워크 오류로 인증 코드 발송에 실패했습니다.");
            });
        } else {
            alert("이메일을 입력해주세요.");
        }
    });
});
