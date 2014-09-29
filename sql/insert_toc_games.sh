for f in ./toc-games/*
do
	echo $f
	mysql -utocuser -ptocpass toc < $f
done