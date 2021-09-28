package org.starcoin.bifrost;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DevNetworkInteractApp {

    public static void main(String[] args) {
        // ------------------------------------
        // 先编译 move 代码：
        // move clean && move publish
        // ------------------------------------
        String starcoinorg_repositories_dir = System.getenv("starcoinorg_repositories_dir");
        String bifrost_repositories_dir = System.getenv("bifrost_repositories_dir");
        if (starcoinorg_repositories_dir == null || bifrost_repositories_dir == null) {
            throw new RuntimeException("starcoinorg_repositories_dir == null || bifrost_repositories_dir == null");
        }
        String shellPath = "/bin/sh";
        String starcoinCmd = starcoinorg_repositories_dir + "/starcoin/target/debug/starcoin -n dev -d alice console";
        String moveProjectDir = bifrost_repositories_dir + "/bifrost-core";
        if (args.length < 1) {
            throw new IllegalArgumentException("Please enter account private key");
        }
        String firstPrivateKey = args[0];
        //String secondPrivateKey = args[1];
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    //new String[] {starcoinCmd, "-n", "dev", "console"}
                    shellPath, "-c", starcoinCmd);
            processBuilder.directory(new File(moveProjectDir));
            //processBuilder.inheritIO();
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if (process == null) {
            throw new NullPointerException();
        }


        CommandLineInteractor commandLineInteractor = new CommandLineInteractor(process);
        commandLineInteractor.expect("Start console,", 10);
        boolean initAccountAndDeploy = false; // fixme: 第一次运行设置这里为 true
        if (initAccountAndDeploy) {
            commandLineInteractor
                    //导入账户，部署合约
                    .sendLine("account import -i " + firstPrivateKey)
                    .expect("\"ok\":", 10)
                    .sendLine("account default 0x569AB535990a17Ac9Afd1bc57Faec683")
                    .expect("\"ok\":", 10)
                    .sendLine("account unlock 0x569AB535990a17Ac9Afd1bc57Faec683")
                    .expect("\"ok\":", 10)
                    //.sendLine("account unlock 0xff2794187d72cc3a9240198ca98ac7b6")
                    //.expect("\"ok\":", 10)
                    .sendLine("dev get-coin 0x569AB535990a17Ac9Afd1bc57Faec683")
                    .expect("\"ok\":", 10)
                    //.sendLine("dev get-coin 0xff2794187d72cc3a9240198ca98ac7b6")
                    //.expect("\"ok\":", 10)
                    .sendLine("dev deploy storage/0x569AB535990a17Ac9Afd1bc57Faec683/modules/Bifrost.mv -b")
                    .expect("\"ok\":", 10)
                    .sendLine("dev deploy storage/0x569AB535990a17Ac9Afd1bc57Faec683/modules/STCTokenBox.mv -b")
                    .expect("\"ok\":", 10)
                    .sendLine("dev deploy storage/0x569AB535990a17Ac9Afd1bc57Faec683/modules/BifrostToken.mv -b")
                    .expect("\"ok\":", 10)
                    .sendLine("dev deploy storage/0x569AB535990a17Ac9Afd1bc57Faec683/modules/BifrostScripts.mv -b")
                    .expect("\"ok\":", 10)
            ;
            // ////////////////////////////////////////////
            commandLineInteractor
                    .sendLine("account unlock 0x569AB535990a17Ac9Afd1bc57Faec683")
                    .expect("\"ok\":", 10)
                    .sendLine("account execute-function -s 0x569AB535990a17Ac9Afd1bc57Faec683 " +
                            "--function 0x569AB535990a17Ac9Afd1bc57Faec683::BifrostScripts::register_token_box " +
                            "-t 0x1::STC::STC " +
                            "-b")
                    .expect("\"ok\":", 10)
            ;
        }
        // /////////////////////////////////////////////
        /*
            跨链编码：
            1 表示以太坊；
            2 表示 BSC；
            3 表示 heco 链；
         */
        commandLineInteractor
                .sendLine("account unlock 0x569AB535990a17Ac9Afd1bc57Faec683")
                .expect("\"ok\":", 10)
                .sendLine("account execute-function -s 0x569AB535990a17Ac9Afd1bc57Faec683 " +
                        "--function 0x569AB535990a17Ac9Afd1bc57Faec683::BifrostScripts::transfer_to_ethereum_chain " +
                        "-t 0x1::STC::STC " +
                        "--arg x\"91BAa5D576519147f9208F7C3097838dA52E2B3F\" " + // 这是个以太坊地址！
                        "--arg 10000000000u128 " + // Send TEN STC
                        "--arg 1u8 " +
                        "-b")
                .expect("\"ok\":", 10)
        ;
//
//                .sendLine("account execute-function -s 0x569AB535990a17Ac9Afd1bc57Faec683 " +
//                        "--function 0x01::PriceOracleScripts::update " +
//                        "-t 0x569AB535990a17Ac9Afd1bc57Faec683::STCUSDT::STCUSDT " +
//                        "--arg 10000000u128 " +
//                        "-b")
//                .expect("\"ok\":", 10)
        // /////////////////////////////////////////////
        //if (true) return;

        // /////////////////////////////////////////////


        // /////////////////////////////////////////////
    }

    private static List<String> readAllLines(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
