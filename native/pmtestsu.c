#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/stat.h>
static int g_puid;

static int executionFailure(char *context)
{
   fprintf(stderr, "su: %s. Error:%s\n", context, strerror(errno));
   return -errno;
}

static int permissionDenied()
{
   // the superuser activity couldn't be started
   printf("su: permission denied\n");
   return 1;
}

int main(int argc, char **argv)
{
// maybe this helps, cyanogen su use this
//	static const char* const unsec_vars[] = {
//		"GCONV_PATH",
//		"GETCONF_DIR",
//		"HOSTALIASES",
//		"LD_AUDIT",
//		"LD_DEBUG",
//		"LD_DEBUG_OUTPUT",
//		"LD_DYNAMIC_WEAK",
//		"LD_LIBRARY_PATH",
//		"LD_ORIGIN_PATH",
//		"LD_PRELOAD",
//		"LD_PROFILE",
//		"LD_SHOW_AUXV",
//		"LD_USE_LOAD_BIAS",
//		"LOCALDOMAIN",
//		"LOCPATH",
//		"MALLOC_TRACE",
//		"MALLOC_CHECK_",
//		"NIS_PATH",
//		"NLSPATH",
//		"RESOLV_HOST_CONF",
//		"RES_OPTIONS",
//		"TMPDIR",
//		"TZDIR",
//		"LD_AOUT_LIBRARY_PATH",
//		"LD_AOUT_PRELOAD",
//		// not listed in linker, used due to system() call
//		"IFS",
//	};
//	const char* const* cp   = unsec_vars;
//	const char* const* endp = cp + sizeof(unsec_vars)/sizeof(unsec_vars[0]);
//	while (cp < endp) {
//		unsetenv(*cp);
//		cp++;
//	}
//
//	// sane value so "am" works
//	setenv("LD_LIBRARY_PATH", "/vendor/lib:/system/lib", 1);

   struct stat stats;
   struct passwd *pw;
   int uid = 0;
   int gid = 0;

   int ppid = getppid();
   char szppid[256];
   sprintf(szppid, "/proc/%d", ppid);
   stat(szppid, &stats);
   g_puid = stats.st_uid;
   
   //get parent process name
   char procstat[255];
   char procname[255];
   int procID;

   sprintf(procstat,"/proc/%d/stat",ppid);

   FILE *procFile = fopen(procstat,"r");
   fscanf(procFile,"%d",&procID);

   fscanf(procFile,"%s",procname);
   fclose(procFile);

   //remove brackets
   char *ps = procname;
   ps++;
   ps[strlen(ps)-1] = 0;

   
   //check process name
   if(strcmp(ps,"com.pmtest") !=0)
	return permissionDenied();
	
   if(setgid(gid) || setuid(uid)) 
      return permissionDenied();

   char *exec_args[argc + 1];
   exec_args[argc] = NULL;
   exec_args[0] = "sh";
   int i;
   for (i = 1; i < argc; i++)
   {
      exec_args[i] = argv[i];
   }
   execv("/system/bin/sh", exec_args);
   return executionFailure("sh");
}

