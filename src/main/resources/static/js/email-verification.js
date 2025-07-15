// 문서가 완전히 로드되고 파싱이 완료되었을 때 발생하는 이벤트
document.addEventListener('DOMContentLoaded', function() {
    // 인증 코드 전송 버튼 클릭 이벤트 핸들러
    document.getElementById("sendVerificationCodeBtn").addEventListener("click", function() {
        const emailInput = document.getElementById("email");
        const email = emailInput.value;

        // 이메일이 유효한지 검사
        if (!emailInput.checkValidity()) {
            // 브라우저의 기본 스타일로 유효성 에러 메시지를 말풍선 형태로 표시
            emailInput.reportValidity();
            // 유효하지 않은 경우 return문을 통해 이메일 인증 프로세스 종료
            return;
        }

        // 이메일이 유효한 경우
        if (email) {
            // fetch API를 사용하여 서버에 이메일 인증 코드를 전송
            fetch("/user/send-verification-code", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                /**
                 * HTTP 요청의 본문에 해당
                 * email: 서버에서 request.getParameter("email")로 이 값을 읽을 수 있음
                 * encodeURIComponent(email): URL 인코딩을 적용하여 특수 문자를 처리
                 */
                body: `email=${encodeURIComponent(email)}`
            })
            // 서버 응답을 처리
            .then(response => {
                console.log("[서버 응답 수신] 상태:", response.status);
                if (response.ok) {
                    console.log("[인증 코드 발송 성공]");
                    alert("인증 코드가 발송되었습니다.");
                } else {
                    // 서버 응답에서 오류 메시지를 파싱하여 표시
                    response.text().then(errorMessage => {
                        console.error("[인증 코드 발송 실패]", errorMessage);
                        alert("인증 코드 발송에 실패했습니다: " + errorMessage);
                    }).catch(() => {
                        console.error("[인증 코드 발송 실패]", "응답 데이터를 읽을 수 없습니다.");
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
