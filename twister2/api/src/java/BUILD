package(default_visibility = ["//visibility:public"])

load("//tools:maven.bzl", "t2_java_lib")

t2_java_lib(
    name = "api-java",
    srcs = glob(["**/*.java"]),
    artifact_name = "Twiter2 API",
    deps = [
        "//twister2/api/src/java/edu/iu/dsc/tws/api/checkpointing:checkpointing-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/comms:comms-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/compute:task-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/config:config-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/data:data-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/dataset:dataset-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/exceptions:exceptions-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/faulttolerance:fault-tolerance-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/net:network-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/resource:resource-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/scheduler:scheduler-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/tset:tset-api-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/util:api-utils-java",
        "//twister2/api/src/java/edu/iu/dsc/tws/api/driver:driver-api-java",
        "//twister2/common/src/java:common-java",
        "//twister2/proto:proto-java",
        "//twister2/proto/utils:proto-utils-java",
        "@com_google_protobuf//:protobuf_java",
        "@maven//:org_apache_arrow_arrow_vector",
        "@maven//:org_apache_arrow_arrow_memory",
        "@maven//:org_apache_arrow_arrow_format",
        "@maven//:com_google_flatbuffers_flatbuffers_java",
        "@maven//:io_netty_netty_buffer",
        "@maven//:io_netty_netty_all",
        "@maven//:io_netty_netty_transport",
        "@maven//:io_netty_netty_transport_native_epoll",
    ],
)
