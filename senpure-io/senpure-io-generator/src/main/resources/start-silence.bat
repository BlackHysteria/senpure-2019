@echo off
set currentPath=%cd%
::echo %currentPath%
set fileName=""
for  %%a in (*) do (
   echo %%a|find /i "senpure-io-generator" >nul && set fileName=%%a
)
if %fileName% =="" (
    echo "û���ҵ������е�jar�ļ�"
    goto :end
)

::echo %fileName%
call java -Dsilence=true -jar %fileName% 
echo 3���رոô���,Ҫ��ֹͣ�밴ctrl + c
ping /n 2 127.1 >nul
echo 2���رոô���,Ҫ��ֹͣ�밴ctrl + c
ping /n 2 127.1 >nul
echo 1���رոô���,Ҫ��ֹͣ�밴ctrl + c
ping /n 2 127.1 >nul

