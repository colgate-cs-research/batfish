package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

load("@batfish//skylark:junit.bzl", "junit_tests")

junit_tests(
    name = "tests",
    srcs = glob([
        "**/*Test.java",
    ]),
    resources = [
        "//projects/batfish/src/test/resources/org/batfish/grammar/f5_bigip_imish/snapshots",
        "//projects/batfish/src/test/resources/org/batfish/grammar/f5_bigip_imish/testconfigs",
    ],
    deps = [
        "//projects/batfish",
        "//projects/batfish:batfish_testlib",
        "//projects/batfish-common-protocol:common",
        "//projects/batfish-common-protocol/src/test/java/org/batfish/datamodel/matchers",
        "//projects/batfish/src/main/java/org/batfish/grammar/f5_bigip_structured",
        "//projects/batfish/src/main/java/org/batfish/representation/f5_bigip",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_antlr_antlr4_runtime",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_hamcrest_hamcrest",
    ],
)
