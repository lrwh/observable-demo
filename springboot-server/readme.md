# jmx
要将 Java Bean 变成 MBean（管理 Bean），可以通过实现 JMX 接口来实现。以下是一些步骤：

1. 创建一个 Java Bean，并确保它符合 Java Bean 规范。
2. 实现 javax.management.DynamicMBean 接口，该接口定义了 MBean 的动态管理行为。
3. 实现 DynamicMBean 接口中的方法，例如 getAttribute、setAttribute、getMBeanInfo 等，以便管理器可以使用这些方法读取和写入 MBean 属性并在运行时识别它们。
4. 将 MBean 注册到 MBean 服务器中，以便在运行时进行管理和监控。


DynamicMBean 接口还需要实现 invoke 方法。invoke 方法用于调用 MBean 上的操作，也称为 MBean 操作或 MBean 方法

## 要将 MyMBean 注册到 JMX 管理中，您需要执行以下步骤：

1. 获取 MBeanServer 实例。MBeanServer 是 JMX 管理的核心组件之一，用于注册和管理 MBean。
1. 创建 ObjectName 对象。ObjectName 表示 MBean 的唯一标识符。
1. 将 MyMBean 实例注册到 MBeanServer 中，使用前面创建的 ObjectName 来标识 MBean


要在代码运行时修改 MyMBean 中的 myField 属性并通过 JMX 管理界面进行查看，您可以执行以下步骤：

1. 获取 MBeanServer 实例，并创建 ObjectName 对象，该对象标识了您要管理的 MBean。
1. 通过 MBeanServerConnection 接口获取对 MBean 的引用。
1. 使用 getAttribute 方法获取属性的当前值，并使用 setAttribute 方法将其设置为新值。
1. 在 JMX 管理界面中查看属性的新值。