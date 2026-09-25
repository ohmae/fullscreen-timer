# Fullscreen Timer

## プロジェクト概要

- **目的**: 全画面で時計・タイマー・ストップウォッチを表示するアプリ
- **主要機能**: 全画面表示・表示色・フォント・画面方向の設定、時刻の表示形式の設定、Intentによる外部操作
- **構成**: `:app` の単一Androidアプリモジュール。ソースは `app/src/main/kotlin/net/mm2d/timer/`
- **アプリID**: `net.mm2d.timer`（debugビルドは `net.mm2d.timer.debug`）

## エージェントへの基本指示

- Google推奨のAndroid開発のベストプラクティスに従うこと
- コードを提案・生成する際は、以下の技術スタックとコーディング規約を必ず遵守すること
- 既存のコードベースのスタイルと設計思想を尊重し、一貫性を保つこと

## 技術スタック

- **言語**: Kotlin
- **SDK**: `minSdk` = 26, `compileSdk` = 37, `targetSdk` = 37
- **ビルド**: Gradle Kotlin DSL、AGPの組み込みKotlin、JDK toolchain 21、Java/Kotlinの出力ターゲット11
- **UIツールキット**: Jetpack Compose / Material 3。メイン・設定・ライセンス画面とダイアログはComposeで実装済み
- **Viewとの相互運用**: ライセンス本文は `AndroidView` 内の `NestedScrollingWebView` で表示。XMLの画面レイアウトや独自テーマ定義はなく、文字列・DrawableなどのXMLリソースは継続利用
- **DI**: Hilt + KSP
- **非同期処理**: Kotlin Coroutines & Flow
- **ローカルデータ保存**: Preferences DataStore（設定・タイマー実行状態・ストップウォッチ実行状態を別ファイルに保存）
- **依存関係**: `gradle/libs.versions.toml` で管理。ComposeライブラリはBOMでバージョンを揃える
- SDK・ビルド設定の詳細は `app/build.gradle.kts`、Gradleのバージョンは `gradle/wrapper/gradle-wrapper.properties` を参照すること

## アーキテクチャと設計方針

- **アーキテクチャパターン**: MVVM + MVI
- **状態管理**: `MainViewModel` / `SettingsViewModel` が `UiState` を `StateFlow` で公開する。Composeでは `collectAsStateWithLifecycle()` で監視すること
- **イベントと副作用**: 操作は `UiEvent` を `onEvent()` に渡し、画面遷移や音の再生などは `UiEffect` のFlow経由でUI側が処理する。Flowの収集はライフサイクルに合わせること
- **責務分担**: ViewModelは状態と処理の調整を担当し、時間計算・開始停止・復元は `main/TimerController` / `StopwatchController` に集約する。時刻取得は注入可能な `TimeProvider` を利用すること
- **永続化**: `settings/` のRepositoryを通じてDataStoreを読み書きする。保存キー・ファイル名・データ形式の変更時は既存データとの互換性とマイグレーションを確認すること
- **UI実装**: 既存の `MainScreen`、`ClockDisplay`、`SettingsScreen`、`dialog/`、`ui/theme/AppTheme` に合わせる。Window操作・画面方向・画面点灯維持などはActivityや既存のヘルパーで扱うこと
- **外部Intent**: `MainLaunchRequestParser` / `MainCommandParser` で解析し、ViewModelへ渡す。`README.md` に記載された `EXTRA_MODE`・`EXTRA_COMMAND`・`EXTRA_TIME`（Long、ミリ秒）の公開仕様を維持すること

## コーディング規約

- **コードフォーマッター**: ktlint
- Googleの[Kotlinスタイルガイド](https://developer.android.com/kotlin/style-guide)に従う
- フォーマットの具体的な設定は `.editorconfig` を優先する（`intellij_idea` スタイル、4スペース、最大120文字、引数がある関数・クラスのシグネチャは複数行）
- スコープ関数は可読性を優先して選択、`apply`と`also`の可読性が同等であれば、`also`を優先すること

## テスト方針

- **配置**: `app/src/test/kotlin/`。既存のCompose UIテストもローカルユニットテストとして実行する
- **ユニットテスト**: JUnit4 / MockK / Truth / kotlinx-coroutines-test。Android APIが必要なテストはRobolectricを利用する
- **UIテスト**: Compose Testing + Robolectric。既存テストは `@Config(sdk = [35])` と `androidx.compose.ui.test.junit4.v2.createComposeRule` を使用
- **時間・非同期処理のテスト**: `TimeProvider` のFake、`MainDispatcherRule`、コルーチンのテストスケジューラーを使い、実時間の待機に依存させないこと
- **テストのルール**:
  - ビジネスロジック追加時には、必ず対応するユニットテストを記述すること。
  - テスト対象の可視性をテストのために変更する場合は、`private` から `internal` に変更し、`@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)` を付与すること。
  - テストメソッド名はテスト対象のあとにスペースを開けて、日本語でテスト内容を説明文とすること（Kotlinのバッククォート記法を使用）。
    - テストクラス名からわかる自明なクラス名部分は、メソッド名から省略すること。
    - 日本語をメソッド名に使用するため、テストクラスには `@Suppress("NonAsciiCharacters")` を付与すること。

## ビルド・検証コマンド

プロジェクトルートでGradle Wrapperを使い、変更内容に応じた検証を実施すること。

| 用途 | コマンド |
| --- | --- |
| debugビルド | `./gradlew :app:assembleDebug` |
| ユニットテスト・Compose UIテスト | `./gradlew :app:testDebugUnitTest` |
| 特定テスト | `./gradlew :app:testDebugUnitTest --tests 'net.mm2d.timer.main.TimerControllerTest'` |
| Android Lint | `./gradlew :app:lintDebug` |
| Kotlinスタイル確認 / 自動整形 | `./gradlew ktlint` / `./gradlew ktlintFormat` |
| 依存関係の差分確認 | `./gradlew dependencyGuard` |
| 依存関係のベースライン更新 | `./gradlew :app:dependencyGuardBaseline` |
| 端末・エミュレーターへのインストール | `./gradlew :app:installDebug` |

- `ktlint` / `ktlintFormat` は `isIgnoreExitValue = true` のため、Gradleの成功表示だけで判断せず、出力された違反も確認すること
- 依存関係を変更した場合は `app/dependencies/releaseRuntimeClasspath.txt` の差分を確認し、意図した変更に合わせてベースラインを更新すること
- UI変更時は必要に応じて実機・エミュレーターでも全画面表示、縦横画面、ダイアログとキーボードの操作を確認すること

## 禁止事項・避けるべきパターン

- Javaコードの新規利用は禁止、必ずKotlinを使用
- 非推奨 (Deprecated) となったAndroid APIやライブラリは使用しない
- UI層（ComposableやActivity）に直接ビジネスロジックを記述しない
- 複雑なロジックはUI層はもちろん、ViewModelの中にも直接は記述せず、ロジックを単独のクラスとして切り出すこと
