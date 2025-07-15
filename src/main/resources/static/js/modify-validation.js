// 회원정보 수정 폼 유효성 검사 스크립트
// 각 입력란에 대해 실시간 및 제출 시 유효성 검사를 수행합니다.

// 문서가 모두 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    // 폼과 입력란 요소들을 변수에 저장
    const form = document.getElementById('modifyForm');
    const newPassword = document.getElementById('newPassword');
    const confirmNewPassword = document.getElementById('confirmNewPassword');
    const currentPassword = document.getElementById('currentPassword');
    const nameInput = document.getElementById('name');
    const userNicknameInput = document.getElementById('userNickname');
    const confirmError = document.getElementById('confirmError');
    const passwordError = document.getElementById('passwordError'); // 비밀번호 유효성 오류 메시지 표시용

    /**
     * 서버에서 전달된 에러 메시지를 각 입력란에 말풍선(브라우저 기본)으로 표시하는 함수
     * (입력란 아래 span에 있는 에러 메시지를 읽어서 setCustomValidity로 입력란에 연결)
     */
    console.log('서버 에러 표시');
    function displayServerErrors() {
        // 현재 비밀번호 에러 처리
        console.log('현재 비밀번호 에러 처리');
        const currentPasswordError = document.querySelector('span[th\\:errors="*{currentPassword}"]');
        if (currentPasswordError && currentPasswordError.textContent.trim()) {
            currentPassword.setCustomValidity(currentPasswordError.textContent.trim());
            currentPasswordError.style.display = 'none'; // 기존 에러 메시지 숨기기
        } else {
            currentPassword.setCustomValidity('');
        }

        // 새 비밀번호 에러 처리
        console.log('새 비밀번호 에러 처리');
        const newPasswordError = document.querySelector('span[th\\:errors="*{newPassword}"]');
        if (newPasswordError && newPasswordError.textContent.trim()) {
            newPassword.setCustomValidity(newPasswordError.textContent.trim());
            newPasswordError.style.display = 'none';
        } else {
            newPassword.setCustomValidity('');
        }

        // 새 비밀번호 확인 에러 처리
        console.log('새 비밀번호 확인 에러 처리');
        const confirmNewPasswordError = document.querySelector('span[th\\:errors="*{confirmNewPassword}"]');
        if (confirmNewPasswordError && confirmNewPasswordError.textContent.trim()) {
            confirmNewPassword.setCustomValidity(confirmNewPasswordError.textContent.trim());
            confirmNewPasswordError.style.display = 'none';
        } else {
            confirmNewPassword.setCustomValidity('');
        }

        // 이름 에러 처리
        console.log('이름 에러 처리');
        const nameError = document.querySelector('span[th\\:errors="*{name}"]');
        if (nameError && nameError.textContent.trim()) {
            nameInput.setCustomValidity(nameError.textContent.trim());
            nameError.style.display = 'none';
        } else {
            nameInput.setCustomValidity('');
        }

        // 닉네임 에러 처리
        console.log('닉네임 에러 처리');
        const userNicknameError = document.querySelector('span[th\\:errors="*{userNickname}"]');
        if (userNicknameError && userNicknameError.textContent.trim()) {
            userNicknameInput.setCustomValidity(userNicknameError.textContent.trim());
            userNicknameError.style.display = 'none';
        } else {
            userNicknameInput.setCustomValidity('');
        }
    }

    /**
     * 이름 입력란 유효성 검사 함수
     * - 값이 비어 있으면 에러 메시지 표시
     */
    function validateName() {
        console.log('이름 유효성 검사');
        const value = nameInput.value;
        if (!value) {
            nameInput.setCustomValidity('이름을 입력해주세요.');
        } else {
            nameInput.setCustomValidity('');
        }
        nameInput.reportValidity(); // 브라우저 말풍선 알림
    }

    /**
     * 닉네임 입력란 유효성 검사 함수
     * - 비어 있거나, 영소문자/숫자가 아니면 에러
     */
    function validateUserNickname() {
        console.log('닉네임 유효성 검사');
        const value = userNicknameInput.value;
        if (!value) {
            userNicknameInput.setCustomValidity('닉네임을 입력해주세요.');
        } else if (/[^a-z0-9]/.test(value)) {
            userNicknameInput.setCustomValidity('닉네임은 영소문자와 숫자만 사용할 수 있습니다.');
        } else {
            userNicknameInput.setCustomValidity('');
        }
        userNicknameInput.reportValidity();
    }

    /**
     * 현재 비밀번호 입력란 유효성 검사 함수
     * - 비어 있으면 에러
     */
    function validateCurrentPassword() {
        console.log('현재 비밀번호 유효성 검사');
        const value = currentPassword.value;
        if (!value) {
            currentPassword.setCustomValidity('현재 비밀번호를 입력해주세요.');
        } else {
            currentPassword.setCustomValidity('');
        }
        currentPassword.reportValidity();
    }

    /**
     * 새 비밀번호 입력란 유효성 검사 함수
     * 8자 미만이면 에러
     */
    function validateNewPassword() {
        console.log('새 비밀번호 유효성 검사');

        const newPasswordValue = newPassword.value;
        const confirmPasswordValue = confirmNewPassword.value;

        // 새 비밀번호를 입력하지 않은 경우
        if (!newPasswordValue) {
            // 아무런 검사도 하지 않음
            newPassword.setCustomValidity('');
            confirmNewPassword.setCustomValidity('');
            return;
        // 새 비밀번호를 입력했는데 8자 미만이면 에러
        } else if (newPasswordValue.length < 8) {
            newPassword.setCustomValidity('새 비밀번호는 8자 이상이어야 합니다.');
        // 새 비밀번호를 입력했는데 8자 이상이면 에러 제거
        } else {
            newPassword.setCustomValidity('');
        }
        
        if (newPasswordValue !== confirmPasswordValue) {
            confirmNewPassword.setCustomValidity('새 비밀번호가 일치하지 않습니다.');
        } else {
            confirmNewPassword.setCustomValidity('');
        }
        newPassword.reportValidity();
        confirmNewPassword.reportValidity();
    }

    // ================= 이벤트 리스너 등록 =================
    // 입력할 때마다 실시간 유효성 검사 (비밀번호 일치 제외)
    console.log('이벤트 리스너 등록');
    nameInput.addEventListener('keyup', validateName);
    userNicknameInput.addEventListener('keyup', validateUserNickname);
    currentPassword.addEventListener('keyup', validateCurrentPassword);
    newPassword.addEventListener('keyup', validateNewPassword);
    // 비밀번호 일치 검사는 폼 제출 시에만 실행

    // ================= 폼 제출 이벤트 처리 =================
    console.log('폼 제출 이벤트 처리');
    form.addEventListener('submit', function(e) {

        // TODO: 새 비밀번호가 입력된 경우만 검사

        // 폼 제출 시 콘솔에 로그
        console.log('수정 폼 제출');
        // 비밀번호 일치 검사는 제출 시에만 실행
        validateConfirmNewPassword();

        // 폼 전체 유효성 검사 (하나라도 invalid면 제출 막음)
        if (!form.checkValidity()) {
            e.preventDefault();
        }
    });

    // ================= 페이지 로드시 서버 에러 표시 =================
    displayServerErrors();
});