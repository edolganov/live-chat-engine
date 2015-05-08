

var hasProps = true;
try {
    Props.toString();
}catch(e){
    //no Props yet
    hasProps = false;
}

if( ! hasProps){
	var Props = {};
}