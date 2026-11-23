@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.
@REM Maven Wrapper - mvnw.cmd
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET DP0=%~dp0
@IF "%MAVEN_WRAPPER_PROPERTIES_FILE%"=="" (@SET MAVEN_WRAPPER_PROPERTIES_FILE="%DP0%.mvn\wrapper\maven-wrapper.properties")
@FOR /F "usebackq tokens=1,* delims==" %%a IN (%MAVEN_WRAPPER_PROPERTIES_FILE%) DO (
    @IF "%%a"=="distributionUrl" @SET distributionUrl=%%b
)
@SET MVN_CMD="%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9-bin"
@FOR /F "tokens=*" %%i IN ('dir /b "%USERPROFILE%\.m2\wrapper\dists" 2^>nul ^| findstr /i "apache-maven-3.9"') DO (
    @FOR /F "tokens=*" %%j IN ('dir /b "%USERPROFILE%\.m2\wrapper\dists\%%i" 2^>nul') DO (
        @IF EXIST "%USERPROFILE%\.m2\wrapper\dists\%%i\%%j\apache-maven-3.9*\bin\mvn.cmd" (
            @SET MVN_EXE="%USERPROFILE%\.m2\wrapper\dists\%%i\%%j\apache-maven-3.9*\bin\mvn.cmd"
        )
    )
)
@IF NOT "%MVN_EXE%"=="" (
    @%MVN_EXE% %*
) ELSE (
    @ECHO Maven wrapper could not find mvn. Please install Apache Maven 3.9+
    @EXIT /B 1
)
