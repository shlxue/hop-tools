# Testing Hop Plugins

To simplify the development and unit testing of Hop plugins,
we implemented `HopExtension` extension based on JUnit 5: By implementing a custom TestTemplate,
it can standardize the code processing of SWT UI.

## Building

### make build

On linux or macOS: `make all`

### mvn build

1. Building build-tools
   * disable extensions: `mv .mvn/extensios.xml .mvn/tmp-extensions.xml`
   * `mvn install -pl :build-tools`
   * restore extensions: `mv .mvn/tmp-extensions.xml .mvn/extensions.xml`
2. Building: `mvn install`
3. Test plugins dialog
    * actions: `mvn verify -Pit -pl :it-action-plugins`
    * transforms: `mvn verify -Pit -pl :it-tranform-plugins`
4. Test single plugin dialog
    * actions: `mvn verify -Pit -pl :it-action-plugins -Dit.test=ActionIT#testActionHttpUi`
    * transforms: `mvn verify -Pit -pl :it-transform-plugins -Dtest=TransformIT#testTextFileInputUi`
5. Ui mode
    * Debug: `mvn verify -Pit -pl :it-action-plugins -Dit.test=ActionIT#testActionSftpPutUi -DHOP_JUNIT_UI=DEBUG`
    * Preview: `mvn verify -Pit -pl :it-action-plugins -Dtest=ActionIT#testXsltUi -DHOP_JUNIT_UI=PREVIEW`
    * Normal: `mvn verify -Pit -pl :it-transform-plugins -Dtest=TransformIT#testXsltUi -DHOP_JUNIT_UI=NORMAL`
    * Headless: `mvn verify -Pit -pl :it-transform-plugins -Dtest=TransformIT#testXsltUi -DHOP_JUNIT_UI=HEADLESS`
6. Delay close
    * Preview: `mvn verify -Pit -pl :it-transform-plugins -Dtest=TransformIT#testRestUi -DHOP_JUNIT_UI=PREVIEW -DHOP_JUNIT_DELAY=30000`
7. SWT & RWT compatibly at building phase
    * SWT `mvn clean install`
    * RWT `mvn clean test-compile -Dxwt.type=rwt`

## Modules

* __build-tools__:
  * dynamic inject SWT library by osgi.platform property
  * Auto switch __SWT&RWT__
  * Append -XstartOnFirstThread jvm option on macOS for surefire & failsafe plugins
* __hop-plugin-testing__: Junit extensions
* __hop-ui-helper__: building swt dialog by flow coding
* example & IT
    * hop-example
    * it-action-plugins
    * it-transform-plugins

## Features

1. [x] H2Extension: embedded h2 database(tcp server)
2. [x] SwtExtension: swt & rwt support
    * [x] Preview Swt
    * [ ] Preview Rwt
3. [x] HopExtension: junit test for hop core plugins
    * [x] Mock
    * [x] HopLocal
    * [ ] BeamDirect
4. [ ] Testing template
    * [x] ui-spec-template: Action Dialog
    * [x] ui-spec-template: Transform Dialog
    * [ ] action-template: action.execute
    * [ ] transform-template: transform.processRow
5. [ ] Switch ui: SWT & RWT at building phase
   * [x] Testing with SWT
   * [ ] Testing with RWT
6. build-tools
   * [x] Compile all modules with SWT&RWT(switch by xwt.type property, ps: `mvn install -Dxwt.type=rwt`)

### About @HopEnv

Switch different runtime environments through `@HopEnv` annotation: `Mock`, `HOP_LOCAL`, `BEAM_DIRECT`

```java
@HopEnv(type = Type.HOP_LOCAL, withH2 = true, ui = SpecMode.HEADLESS)
```

### HOP_JUNIT_xxx

Environment variable `HOP_JUNIT_xxx`: `mvn test -DHOP_JUNIT_TYPE=MOCK -DHOP_JUNIT_UI=PREVIEW -DHOP_JUNIT_DEPLOY=30000`

## Example

```java
// the simplest test
@ExtendWith(HopExtension.class)
class Demo1Test {
    @TestTemplate
    void testTransformUi(Demo1Dialog dialog) {
        Assertions.assertNotNull(dialog);
    }
}
```

```java
@ExtendWith(HopExtension.class)
@HopEnv(ui = SpecMode.PREVIEW)
class Demo1Test {
}

```

## hop-ui-helper

[^junit-parallel]: Don't support junit parallel: `junit.jupiter.execution.parallel.enabled=true`
