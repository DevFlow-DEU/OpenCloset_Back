# 카카오 회원가입 추가 정보 API

1. `GET /auth/kakao/login-url`로 카카오 인증을 시작합니다.
2. `GET /auth/kakao/callback?code=...` 응답의 `signupRequired`가 `true`이면 추가 정보 입력 화면을 표시합니다. `false`이면 로그인 완료입니다.
3. 닉네임 확인 버튼은 기존 `GET /auth/check_nickname?nickname=...`을 사용합니다. 최종 저장 시에도 중복을 검사합니다. 임시로 부여된 본인 닉네임은 최종 저장에서 허용합니다.
4. 현재 위치 버튼은 프론트의 위치 권한 및 주소 변환 기능으로 처리하고, 변환된 주소 문자열을 아래 요청에 넣습니다. 서버는 GPS 좌표를 주소로 변환하거나 위치를 인증하지 않습니다.
5. 콜백에서 받은 `accessToken`을 사용하여 추가 정보를 저장합니다.

```http
POST /auth/kakao/register
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "nickname": "옷친구",
  "address": "서울특별시 강남구 역삼동",
  "password": "password123!",
  "passwordConfirm": "password123!"
}
```

성공: HTTP 200, `{ "email": "...", "nickname": "옷친구", "address": "서울특별시 강남구 역삼동", "profileImage": "..." }`.
기존 accessToken과 refreshToken을 계속 사용하며, 다음 카카오 로그인부터 `signupRequired`는 `false`입니다.

실패: 필수값 누락, 닉네임 중복, 비밀번호 불일치, 일반 계정 요청, 추가 정보 재등록은 HTTP 400입니다. JWT가 없거나 유효하지 않으면 HTTP 401입니다.
닉네임과 주소는 앞뒤 공백 제거 후 1~255자, 비밀번호는 8자 이상 및 UTF-8 기준 72바이트 이하이며 BCrypt로 저장됩니다.

## DB 및 기존 계정

`users`에 nullable `kakao_id`(unique), `kakao_signup_completed` 컬럼이 추가됩니다. 현재 프로젝트의 `ddl-auto=update` 설정으로 반영됩니다. 운영에서 자동 스키마 변경을 끄는 경우 별도 마이그레이션이 필요합니다.

이전 코드는 카카오 ID를 users에 저장하지 않고 이메일로 계정을 찾았습니다. 새 코드는 카카오 ID로 로그인하며 이메일만 같은 계정을 자동으로 연결하지 않습니다. 기존 카카오 계정이 있으면 배포 전에 검증된 카카오 ID를 해당 users 행의 kakao_id에 이관해야 합니다. 추가 정보 입력이 필요한 계정의 kakao_signup_completed는 false, 이미 완료한 계정은 true로 설정합니다. 카카오 ID를 검증할 근거 없이 이메일만으로 연결하지 마세요.

기존 인증 정책을 유지하므로 추가 정보를 입력하기 전에도 콜백에서 일반 JWT가 발급됩니다. `signupRequired`는 화면 전환용 상태이며 서버 전체 기능의 접근 제한을 의미하지 않습니다.
