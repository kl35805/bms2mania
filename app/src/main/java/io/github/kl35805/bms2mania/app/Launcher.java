package io.github.kl35805.bms2mania.app;

import javafx.application.Application;

/**
 * 프로그램 시작점.
 * <p>
 * {@link Application}을 상속하지 않는 별도 클래스에서 실행해야,
 * JavaFX를 모듈 경로가 아닌 클래스패스에 둔 환경(IDE 실행, 단일 jar 등)에서도
 * "JavaFX runtime components are missing" 오류 없이 시작할 수 있다.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
