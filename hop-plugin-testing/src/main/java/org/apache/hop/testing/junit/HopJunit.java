package org.apache.hop.testing.junit;

import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.HopClientEnvironment.ClientType;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.annotations.Action;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.auth.AuthenticationConsumerPluginType;
import org.apache.hop.core.auth.AuthenticationProviderPluginType;
import org.apache.hop.core.compress.CompressionPluginType;
import org.apache.hop.core.config.plugin.ConfigPluginType;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.database.DatabasePluginType;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.plugins.*;
import org.apache.hop.core.util.ExecutorUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.execution.plugin.ExecutionInfoLocationPluginType;
import org.apache.hop.imp.ImportPluginType;
import org.apache.hop.metadata.api.IHasHopMetadataProvider;
import org.apache.hop.metadata.api.IHopMetadata;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.metadata.serializer.memory.MemoryMetadataProvider;
import org.apache.hop.metadata.serializer.multi.MultiMetadataProvider;
import org.apache.hop.metadata.util.HopMetadataUtil;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engine.IPipelineEngine;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.testing.HopEnv;
import org.apache.hop.ui.core.metadata.MetadataManager;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class HopJunit implements AutoCloseable, IHasHopMetadataProvider {
  public static Namespace HOP_NS = Namespace.create("HOP_JUNIT");

  private final HopEnv.Type envType;
  private final IVariables variables;
  private final HopJunitConfig config;
  private final ILogChannel log;

  private final SwtContext swtContext;
  private Shell shell;

  private final List<DatabaseMeta> databaseMetas;
  private IHopMetadataProvider databaseMetaProvider;
  private MultiMetadataProvider metadataProvider;
  private MetaDelegate metaDelegate;
  private PluginUiDelegates uiDelegates;
  private HopGui hopGui;
  private final CountDownLatch lazyLoading;

  public HopJunit(HopEnv.Type type, boolean withH2) {
    this.envType = type;
    this.variables = new Variables();
    this.config = new HopJunitConfig(variables);
    this.log = new LogChannel("HopJunit", false);
    if (!withH2) {
      this.metadataProvider = HopMetadataUtil.getStandardHopMetadataProvider(variables);
    }
    this.databaseMetaProvider = new MemoryMetadataProvider();
    this.metaDelegate = new MetaDelegate(metadataProvider, this::waitUntilLoaded);
    this.swtContext = SwtContext.getInstance();
    this.hopGui = HopGui.getInstance();
    hopGui.setVariables(variables);
    hopGui.setDatabaseMetaManager(
        new MetadataManager<>(
            variables, databaseMetaProvider, DatabaseMeta.class, swtContext.getShell()));
    this.databaseMetas = new ArrayList<>();
    this.lazyLoading = new CountDownLatch(1);
    ExecutorUtil.getExecutor().submit(this::initHopEnvironment);
  }

  public IVariables getVariables() {
    return variables;
  }

  public ILogChannel getLog() {
    return log;
  }

  public SwtContext getSwtContext() {
    return swtContext;
  }

  public Shell getShell() {
    return shell;
  }

  public void setShell(Shell shell) {
    this.shell = shell;
  }

  public void waitUntilLoaded() {
    if (lazyLoading.getCount() == 0) {
      return;
    }
    try {
      for (int i = 0; i < 30; i++) {
        if (lazyLoading.await(1, TimeUnit.SECONDS)) {
          return;
        }
      }
      throw new IllegalStateException("Timed out waiting for plugin load");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void initHopEnvironment() {
    HopClientEnvironment.getInstance().setClient(ClientType.OTHER);
    HopClientEnvironment.getInstance().setClientID("HOP-JUNIT");
    try {
      initHopLogStore();
      log.logBasic("Initializing Hop Junit...");
      try {
        HopClientEnvironment.init();
      } catch (Throwable e) {
        log.logError("Error initializing Hop Junit.", e);
      }
      initHopPlugins(new ArrayList<>(HopEnvironment.getStandardPluginTypes()));
      if (HopEnvironment.isInitialized()) {
        PluginRegistry registry = PluginRegistry.getInstance();
        searchPlugins(
                ClassFilter.of(ITransformMeta.class::isAssignableFrom),
                registry.getPlugins(TransformPluginType.class))
            .forEach(
                clazz -> registerPluginClass(Transform.class, TransformPluginType.class, clazz));
        searchPlugins(
                ClassFilter.of(IAction.class::isAssignableFrom),
                registry.getPlugins(ActionPluginType.class))
            .forEach(clazz -> registerPluginClass(Action.class, ActionPluginType.class, clazz));
      }
      //      HopGui.getInstance().setDatabaseMetaManager(
      //      new MetadataManager<>(variables, metadataProvider, DatabaseMeta.class,
      // swtContext.getShell()));
    } catch (HopException e) {
      throw new RuntimeException(e);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    } finally {
      lazyLoading.countDown();
      if (HopEnvironment.isInitialized()) {
        PluginRegistry registry = PluginRegistry.getInstance();
        log.logDebug(
            "Loading plugins: databases={0}, actions={1}, transforms={2}",
            registry.getPlugins(DatabasePluginType.class).size(),
            registry.getPlugins(ActionPluginType.class).size(),
            registry.getPlugins(TransformPluginType.class).size());
      }
    }
  }

  private List<Class<?>> searchPlugins(ClassFilter filter, List<IPlugin> exist) {
    List<Class<?>> types =
        new ArrayList<>(
            ReflectionUtils.findAllClassesInClasspathRoot(config.getClassPath().toUri(), filter));
    //    for (URI jar : config.getHopPluginJars()){
    //      types.addAll(ReflectionUtils.findAllClassesInClasspathRoot(jar, filter));
    //    }
    types.removeIf(
        clazz ->
            exist.stream().anyMatch(plugin -> plugin.getClassMap().containsValue(clazz.getName())));
    return types;
  }

  private <T extends Annotation, P extends IPluginType<T>> void registerPluginClass(
      Class<T> type, Class<P> pluginType, Class<?> clazz) {
    try {
      PluginRegistry.getInstance().registerPluginClass(clazz.getName(), pluginType, type);
    } catch (HopPluginException e) {
      log.logMinimal("Unable to register {0} plugin: {0}", pluginType.getSimpleName(), clazz, e);
    }
  }

  public <T extends IHopMetadata> void setup(Class<T> type, T value) {
    try {
      if (metadataProvider == null) {
        metadataProvider = new MultiMetadataProvider(variables, databaseMetaProvider);
        this.metaDelegate = new MetaDelegate(metadataProvider, this::waitUntilLoaded);
        this.uiDelegates = new PluginUiDelegates(this, metaDelegate);
        hopGui.getMetadataProvider().setProviders(metadataProvider.getProviders());
      }
      //      hopGui.getMetadataProvider().getSerializer(type).save(value);
      metadataProvider.getSerializer(type).save(value);
    } catch (HopException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public MultiMetadataProvider getMetadataProvider() {
    return metadataProvider;
  }

  @Override
  public void setMetadataProvider(MultiMetadataProvider multiMetadataProvider) {}

  public IVariables newVariables() {
    IVariables variables = new Variables();
    variables.setParentVariables(this.variables);
    return variables;
  }

  public <T extends IAction> T newAction(Class<T> clazz) {
    waitUntilLoaded();
    return metaDelegate.newAction(clazz);
  }

  public <T extends ITransform> T newTransform(Class<T> clazz) {
    return metaDelegate.newTransform(clazz);
  }

  public <M extends PipelineMeta, T extends IPipelineEngine<M>> T newEngine(
      Class<T> clazz, M meta) {
    return metaDelegate.newEngine(clazz, meta);
  }

  public <M extends WorkflowMeta, T extends IWorkflowEngine<M>> T newEngine(
      Class<T> clazz, M meta) {
    return metaDelegate.newEngine(clazz, meta);
  }

  public <T extends ITransformMeta> T newTransformMeta(Class<T> meteType) {
    waitUntilLoaded();
    return metaDelegate.newTransformMeta(meteType);
  }

  @Override
  public void close() {
    if (HopEnvironment.isInitialized()) {
      HopEnvironment.reset();
    }
    if (HopClientEnvironment.isInitialized()) {
      synchronized (HopLogStore.class) {
        HopClientEnvironment.reset();
      }
    }
    initHopLogStore();
  }

  private void initHopPlugins(List<IPluginType> pluginTypes) throws HopException {
    if (envType.isMock()) {
      pluginTypes.removeIf(this::nonMockPluginType);
    } else if (envType.isBeam()) {
      pluginTypes.removeIf(this::nonBeamPluginType);
    } else {
      pluginTypes.removeIf(this::nonHopLocalPluginType);
    }
    if (!pluginTypes.isEmpty()) {
      log.logDebug("Initializing Hop Environment with {} plugins", pluginTypes.size());
      HopEnvironment.init(pluginTypes);
    }
  }

  private boolean nonBeamPluginType(IPluginType<?> pluginType) {
    return Stream.of(HopServerPluginType.class, ImportPluginType.class)
        .anyMatch(type -> type.equals(pluginType.getClass()));
  }

  private boolean nonHopLocalPluginType(IPluginType<?> pluginType) {
    return Stream.of(
            HopServerPluginType.class,
            ImportPluginType.class,
            ExecutionInfoLocationPluginType.class)
        .anyMatch(type -> type.equals(pluginType.getClass()));
  }

  private boolean nonMockPluginType(IPluginType<?> pluginType) {
    return Stream.of(
            HopServerPluginType.class,
            CompressionPluginType.class,
            AuthenticationProviderPluginType.class,
            AuthenticationConsumerPluginType.class,
            ConfigPluginType.class,
            ImportPluginType.class,
            ExecutionInfoLocationPluginType.class)
        .anyMatch(type -> type.equals(pluginType.getClass()));
  }

  private void initHopLogStore() {
    if (!HopLogStore.isInitialized()) {
      log.logDebug("Initializing Hop Log Store");
      HopLogStore.init(false, true);
    }
  }
}
