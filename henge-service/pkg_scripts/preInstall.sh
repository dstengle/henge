#=====================================================================
# desc: preinstall.sh: steps to be executed before henge.deb 
#     file is installed
# prereqs: henge user exists, java 1.8.x is installed on 
#     path and accessible via java cmd
#=====================================================================


user_exists(){
   #print true or false depending on whether user was found in /etc/passwd file
   #todo: take param and check for user=$1, right now hard-coded for henge user
   #todo: make this not rely on python?

   username="$1" 

   grep ${username} /etc/passwd

   exit_val=$?
   case $exit_val in 
       (0)     bool_exists="true" ;;
       (1)     bool_exists="false"
   esac

   printf $bool_exists
}
#==================================================================
issubstring() {

    #Usage: issubstring substr fullstr
    #print true if substr is in fullstr, otherwise false

    substring="$1"
    full_string="$2"

    case $2 in 
        (*$1*) printf true ;;
        (*)     printf false
    esac

}

#==================================================================

echo "henge: checking prereqs"

#ensure user exists.  If she does not exist, create her. 
#User is needed by upstart script to run service
ps_user="henge"
#user_requirement_met=$(user_exists $ps_user)

# check that user exists
#if [ "$user_requirement_met" = 'false' ]
#then
#  echo "creating user: $ps_user"
#  useradd $ps_user 
#else
#  echo "user exists. no need to add: $ps_user"  
#fi


useradd $ps_user
printf "adding user: $ps_user"

home_dir="/home/$ps_user"
printf "making home dir: $home_dir"
mkdir $home_dir
chown ${ps_user}:${ps_user} $home_dir


#===================================================================
# check for Java installation and version == 1.8
# todo: check for >= 1.8.x this checks for only version 1.8.x

if type java
then

    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')

    correct_java_version=$(issubstring '1.8' "$java_version")

    if [ "$correct_java_version" = "true" ]
    then 
        printf "Java version: $java_version"
        printf "Java version requirement met"
    else
        printf "incorrect java version: must be 1.8.x.  Your version: $java_version" 
        exit 1

    fi

else
    echo "Java not found.  Please ensure that the java command is available to user: $ps_user"
    exit 1
fi


#=================================================


