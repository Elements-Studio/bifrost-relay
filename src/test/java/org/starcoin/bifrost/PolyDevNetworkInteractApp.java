package org.starcoin.bifrost;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PolyDevNetworkInteractApp {

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
        String moveProjectDir = bifrost_repositories_dir + "/poly-stc-contracts";
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
        boolean initAccountAndDeploy = true; // fixme: 第一次运行设置这里为 true
        if (initAccountAndDeploy) {
            commandLineInteractor
                    //导入账户，部署合约
//                    .sendLine("account import -i " + firstPrivateKey)
//                    .expect("\"ok\":", 10)
                    .sendLine("account default 0x569AB535990a17Ac9Afd1bc57Faec683")
                    .expect("\"ok\":", 10)
                    .sendLine("account unlock 0x569AB535990a17Ac9Afd1bc57Faec683")
                    .expect("\"ok\":", 10)
//                    //.sendLine("account unlock 0xff2794187d72cc3a9240198ca98ac7b6")
//                    //.expect("\"ok\":", 10)
//                    .sendLine("dev get-coin 0x569AB535990a17Ac9Afd1bc57Faec683")
//                    .expect("\"ok\":", 10)
//                    //.sendLine("dev get-coin 0xff2794187d72cc3a9240198ca98ac7b6")
//                    //.expect("\"ok\":", 10)
//                    .sendLine("dev deploy storage/0x569AB535990a17Ac9Afd1bc57Faec683/modules/Address.mv -b")
//                    .expect("\"ok\":", 10)
//                    .sendLine("dev deploy storage/0x569AB535990a17Ac9Afd1bc57Faec683/modules/CrosschainType.mv -b")
//                    .expect("\"ok\":", 10)
//                    .sendLine("dev deploy storage/0x569AB535990a17Ac9Afd1bc57Faec683/modules/CrosschainUtils.mv -b")
//                    .expect("\"ok\":", 10)
//                    .sendLine("dev deploy storage/0x569AB535990a17Ac9Afd1bc57Faec683/modules/CrosschainGlobal.mv -b")
//                    .expect("\"ok\":", 10)
                    // ------------------------------------
                    // 使用 package 的方式部署
                    .sendLine("dev package -o ./build -n packaged ./storage/0x2d81a0427d64ff61b11ede9085efa5ad/")
                    .expect("\"ok\":", 10)
                    .sendLine("dev deploy ./build/packaged.blob -b")
                    .expect("\"ok\":", 10)
                    // ------------------------------------
            ;

        }

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
