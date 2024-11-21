# Testing Hop Plugins

To simplify the development and unit testing of Hop plugins,
we implemented `HopExtension` extension based on JUnit 5: By implementing a custom TestTemplate,
it can standardize the code processing of SWT UI.

## Building

1. Config swt
    * windows: `echo '-Dosgi.platform=win32.win32.x86_64' > .mvn/maven.config`
    * linux: `echo '-Dosgi.platform=gtk.linux.aarch64' > .mvn/maven.config`
    * macOS: `echo '-Dosgi.platform=cocoa.macosx.aarch64' > .mvn/maven.config`
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

## Modules

* hop-plugin-testing
* hop-ui-helper
* example & IT
    * hop-example
    * it-action-plugins
    * it-transform-plugins

## Features

1. [x] HopExtension: junit test for hop core plugins
    * [x] Mock & HopLocal
    * [ ] BeamDirect
2. [x] SwtExtension: swt support
3. [ ] H2Extension: embedded h2 database(tcp server)

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
