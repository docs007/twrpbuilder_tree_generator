package mkTree;

import java.io.File;

import util.Clean;
import util.FWriter;
import util.GetBuildInfo;
import util.ShellExecuter;

public class MkFstab {
	private String compressionType;
	private boolean lz4,lzma;
	private String idata=ShellExecuter.CopyRight();
	public MkFstab() {
		System.out.println("Copying fstab");
		compressionType=ShellExecuter.commandnoapp("cd out && file --mime-type recovery.img-ramdisk.* | cut -d / -f 2 | cut -d '-' -f 2");
		if(compressionType.equals("lzma"))
		{
			System.out.println("Found lzma comression in ramdisk");
			ShellExecuter.command("mv out/recovery.img-ramdisk.gz out/recovery.img-ramdisk.lzma && lzma -d out/recovery.img-ramdisk.lzma  && cd out && cpio -i <recovery.img-ramdisk");
			lzma=true;
			CheckCompression();
			lastMessage();
		}else if(compressionType.equals("gzip"))
		{
			System.out.println("Found gzip comression in ramdisk");
			ShellExecuter.command("gzip -d out/recovery.img-ramdisk.gz && cd out && cpio -i <recovery.img-ramdisk");
			lastMessage();
		}
		else if(compressionType.equals("lz4"))
		{
			System.out.println("Found lz4 comression in ramdisk");
		ShellExecuter.command("lz4 -d out/recovery.img-ramdisk.* && cd out && cpio -i <recovery.img-ramdisk");
		lz4=true;
		CheckCompression();
		lastMessage();
		}else {
			new Clean();
			System.out.println("failed to uncompress ramdisk");
			System.exit(0);
		}
		
	}
	
	private void lastMessage() {
		if(new File("out/etc").exists()) {
		ShellExecuter.command("mkdir "+GetBuildInfo.getPathS()+"stock && mv out/etc/* "+GetBuildInfo.getPathS()+"stock/");
		Fstab("out/etc/recovery.fstab");
		}
		System.out.println("Build fingerPrint: "+GetBuildInfo.getFingerPrint());
		System.out.println("tree ready for "+ GetBuildInfo.getCodename());
		System.out.println((char)27 + "[31m" +"Waring :- Check recovery fstab before build");
	}
	
	private void Fstab(String path)
	{
		makeFstab(grepPartition(path,"boot"));
		makeFstab(grepPartition(path,"recovery"));
		makeFstab(grepPartition(path,"system"));
		makeFstab(grepPartition(path,"data"));
		makeFstab(grepPartition(path,"cache"));
		makeFstab(grepPartition(path,"fotakernel"));
		makeFstab("/dev/block/mmcblk1p1");
	}
	
	private void makeFstab(String pPath) {
		if(pPath.endsWith("boot") || pPath.endsWith("BOOT") || pPath.endsWith("Boot"))
		{
			idata+="/boot emmc "+pPath+"\n";
		}
		
		if(pPath.endsWith("recovery") || pPath.endsWith("RECOVERY") || pPath.endsWith("Recovery"))
		{
			idata+="/recovery emmc "+pPath+"\n";
		}
		
		if(pPath.endsWith("system") || pPath.endsWith("SYSTEM") || pPath.endsWith("System") || pPath.endsWith("emmc@android"))
		{
			idata+="/system ext4 "+pPath+"\n";
		}
		
		if(pPath.endsWith("data") || pPath.endsWith("DATA") || pPath.endsWith("Data"))
		{
			idata+="/data ext4 "+pPath+"\n";
		}
		
		if(pPath.endsWith("cache") || pPath.endsWith("CACHE") || pPath.endsWith("Cache"))
		{
			idata+="/cache ext4 "+pPath+"\n";
		}
		
		if(pPath.endsWith("mmcblk1p1"))
		{
			idata+="/ext_sd vfat /dev/block/mmcblk1p1 /dev/block/mmcblk1 flags=display=\"Micro SDcard\";storage;wipeingui;removable";
		}
		
		if(pPath.endsWith("FOTAKernel") || pPath.endsWith("fotakernel"))
		{
			idata+="/recovery ext4 "+pPath+"\n";	
		}
		
		new FWriter("recovery.fstab",idata);
	}
	
	private String grepPartition(String path,String partition) {
			String s =ShellExecuter.commandnoapp("for i in $(cat "+path+" | grep -wi /"+partition+")\n" + 
					"do\n" + 
					"a=$(echo $i | grep /dev)\n" + 
					"echo $a\n" + 
					"done");
			
			if(s.equals(""))
			{
				s =ShellExecuter.commandnoapp("for i in $(cat "+path+" | grep -wi /"+partition+")\n" + 
						"do\n" + 
						"a=$(echo $i | grep /emmc)\n" + 
						"echo $a\n" + 
						"done");
			}
		return s;
	}
	
	private void CheckCompression() {
		String idata=null;
		if(lzma==true)
		{
		System.out.println("using lz4 custom boot  ");
		idata+="BOARD_CUSTOM_BOOTIMG_MK := device/generic/twrpbuilder/mkbootimg_lzma.mk";	
		}
		if(lz4==true)
		{
		System.out.println("using lz4 custom boot  ");
		idata+="BOARD_CUSTOM_BOOTIMG_MK := device/generic/twrpbuilder/mkbootimg_lz4.mk";
		}
		if(idata!=null)
		{
			ShellExecuter.command("echo "+idata +" >> " +GetBuildInfo.getPath()+"/kernel.mk");
		}
	}
}
