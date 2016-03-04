package com.kuassivi.compiler;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import com.kuassivi.annotation.RepositoryCacheManager;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
//@formatter:off
public class RepositoryCacheTest {

    @Test
    public void generatedProxyWith1EmptyMethod_isCorrect() throws Exception {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test",
            Joiner.on('\n').join(
                "package test;",
                "import com.kuassivi.annotation.RepositoryCache;",
                "public interface Test {",
                "  @RepositoryCache String getBar();",
                "}"
        ));

        String md5 = RepositoryCacheManager.hashMD5("Test_getBar");

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestProxyCache",
            Joiner.on('\n').join(
                ProxyCodeTestGenerator.joinItems(
                    "   public static TestProxyCache getBar(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5 + "\", 0);",
                    "   }"
                )
         ));

        assertAbout(javaSource()).that(source)
                 .processedWith(new RepositoryCacheProcessor())
                 .compilesWithoutError()
                 .and()
                 .generatesSources(
                         expectedSource);
    }

    @Test
    public void generatedProxyWith2Methods_isCorrect() throws Exception {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test",
            Joiner.on('\n').join(
                    "package test;",
                    "import com.kuassivi.annotation.RepositoryCache;",
                    "public interface Test {",
                    "  @RepositoryCache(1000 * 60 * 60) String getFoo(String param);",
                    "  @RepositoryCache String getBar();",
                    "}"
            ));

        String md5_foo = RepositoryCacheManager.hashMD5("Test_getFoo-java.lang.String");
        String md5_bar = RepositoryCacheManager.hashMD5("Test_getBar");

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestProxyCache",
            Joiner.on('\n').join(
                ProxyCodeTestGenerator.joinItems(
                    "   public static TestProxyCache getFoo(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5_foo + "\", 3600000);",
                    "   }",

                    "   public static TestProxyCache getBar(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5_bar + "\", 0);",
                    "   }"
                )
            ));

        assertAbout(javaSource()).that(source)
             .processedWith(new RepositoryCacheProcessor())
             .compilesWithoutError()
             .and()
             .generatesSources(expectedSource);
    }

    @Test
    public void generatedProxyWith2MethodsWithSameName_fail() throws Exception {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test",
            Joiner.on('\n').join(
                    "package test;",
                    "import com.kuassivi.annotation.RepositoryCache;",
                    "public interface Test {",
                    "  @RepositoryCache String getBar();",
                    "  @RepositoryCache String getBar(String param);",
                    "}"
            ));

        assertAbout(javaSource()).that(source)
             .processedWith(new RepositoryCacheProcessor())
             .failsToCompile()
             .withErrorContaining(
                      "Conflict: The method \"getBar\" already exists in TestProxyCache.class\n"
                    + "  The checked method in conflict is getBar( java.lang.String ).\n"
                    + "  We cannot process overloaded methods, so please add the \"named\" "
                    + "attribute in the annotated method like RepositoryCache(named = \"getBar_2\").")
             .in(source).onLine(5);
    }

    @Test
    public void generatedProxyWith8Methods_isCorrect() throws Exception {

        JavaFileObject source = JavaFileObjects.forSourceString("test.Test",
            Joiner.on('\n').join(
                    "package test;",
                    "import com.kuassivi.annotation.RepositoryCache;",
                    "public interface Test {",
                    "  @RepositoryCache(named = \"getFooX\")                String getFoo();",
                    "  @RepositoryCache(0)                                  String getFoo(String a);",
                    "  @RepositoryCache(value = 1000)                       String getFoo1(int a, int b, char[] c);",
                    "  @RepositoryCache(-1000)                              String getFoo2(Object... a);",
                    "  @RepositoryCache()                                   String getFoo3();",
                    "  @RepositoryCache                                     String getFoo4();",
                    "  @RepositoryCache(value = 500, named = \"getFooX2\")  String getFoo5();",
                    "  @RepositoryCache(1)                                  String getFoo6();",
                    "}"
            ));

        String md5_foo1 = RepositoryCacheManager.hashMD5("Test_getFooX");
        String md5_foo2 = RepositoryCacheManager.hashMD5("Test_getFoo-java.lang.String");
        String md5_foo3 = RepositoryCacheManager.hashMD5("Test_getFoo1-int-int-char[]");
        String md5_foo4 = RepositoryCacheManager.hashMD5("Test_getFoo2-java.lang.Object[]");
        String md5_foo5 = RepositoryCacheManager.hashMD5("Test_getFoo3");
        String md5_foo6 = RepositoryCacheManager.hashMD5("Test_getFoo4");
        String md5_foo7 = RepositoryCacheManager.hashMD5("Test_getFooX2");
        String md5_foo8 = RepositoryCacheManager.hashMD5("Test_getFoo6");

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestProxyCache",
            Joiner.on('\n').join(
                ProxyCodeTestGenerator.joinItems(
                    "   public static TestProxyCache getFooX(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5_foo1 + "\", 0);",
                    "   }",
                    "   public static TestProxyCache getFoo(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5_foo2 + "\", 0);",
                    "   }",
                    "   public static TestProxyCache getFoo1(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5_foo3 + "\", 1000);",
                    "   }",
                    "   public static TestProxyCache getFoo2(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5_foo4 + "\", -1000);",
                    "   }",
                    "   public static TestProxyCache getFoo3(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5_foo5 + "\", 0);",
                    "   }",
                    "   public static TestProxyCache getFoo4(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5_foo6 + "\", 0);",
                    "   }",
                    "   public static TestProxyCache getFooX25(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5_foo7 + "\", 500);",
                    "   }",
                    "   public static TestProxyCache getFoo6(File cacheDir) {",
                    "       return new TestProxyCache(cacheDir, \"" + md5_foo8 + "\", 1);",
                    "   }"
                )
            ));

        assertAbout(javaSource()).that(source)
             .processedWith(new RepositoryCacheProcessor())
             .compilesWithoutError()
             .and()
             .generatesSources(expectedSource);
    }
}
//@formatter:on