Mini-compilateur----------------
Url     : http://codes-sources.commentcamarche.net/source/31820-mini-compilateurAuteur  : Bel0Date    : 05/08/2013
Licence :
=========

Ce document intitul? ? Mini-compilateur ? issu de CommentCaMarche
(codes-sources.commentcamarche.net) est mis ? disposition sous les termes de
la licence Creative Commons. Vous pouvez copier, modifier des copies de cette
source, dans les conditions fix?es par la licence, tant que cette note
appara?t clairement.

Description :
=============

Ce code a &eacute;t&eacute; &eacute;crit dans le cadre d'un cours sur les langag
ues et compilateur. Il contient un analyseur lexical qui lit dans un fichier d'e
ntr&eacute;e et renvoit une suite d'unit&eacute; lexical repr&eacute;sentant le 
fichier source. Il y a ensuite une analyseur syntaxique qui lit une grammaire LL
(1) dans le fichier grammar.txt dans le dossier syntax et qui g&eacute;n&egrave;
re un arbre syntaxique. Cette arbre syntaxique est ensuite traduit dans une repr
&eacute;sentation interm&eacute;diaire du code &agrave; l'aide de la classe SLIP
Generator. Enfin, &agrave; partir de l'arbre en repr&eacute;sentation, on g&eacu
te;n&egrave;re le code final, code qui ressemble assez &agrave; de l'assembleur.
 En fait, ce code s'ex&eacute;cute sur une petite machine virtuelle programm&eac
ute;e en java mais, &eacute;tant donn&eacute; que ce n'est pas moi qui l'ai cod&
eacute;e, je ne l'inclus pas dans le code. Le projet contient aussi une classe q
ui permet de v&eacute;rifier qu'une grammaire est bien LL(1).
<br />
<br />Le 
langage en lui m&ecirc;me est assez basique avec un syntaxe proche de Java (tr&e
grave;s proche meme :)). Il permet quand m&ecirc;me de faire des appels de fonct
ions, des boucles, des tests (imbriqu&eacute;es &eacute;ventuellement), des fonc
tions r&eacute;cursives. Il permet aussi de g&eacute;rer des objets tr&egrave;s 
basiques qui fonctionne en utilisant un niveau (Pour avoir une meilleur id&eacut
e;e de ce qu'on peut faire avec des objets, voir le dossier testSLIP).
<br />

<br />Ce projet est fonctionnel mais il doit encore y avoir quelques probl&egrav
e;mes &agrave; gauche et &agrave; droite surtout dans la partie de la traduction
 de l'arbre syntaxique en arbre en repr&eacute;sentation interne (la g&eacute;n&
eacute;ration de code est loin d'&ecirc;tre optimale).
<br />
<br />Enfin, pou
r ceux qui se demanderait ce qui signifie 'SLIP', c'est l'abbr&eacute;viation po
ur &quot;Simple Language with Integer and Pointer&quot;.
<br />
<br />Si vous 
avez des questions -&gt; commentaires :)
<br /><a name='conclusion'></a><h2> Co
nclusion : </h2>

<br />BUG: aucun ( en esp&eacute;rant ne pas avoir &agrave; m
ettre &agrave; jour cette partie :p )
