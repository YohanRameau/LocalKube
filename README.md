# LocalKube
Le but de l'application LocalKube est de permettre le déploiement et le contrôle d'applications écrite en Java à l'intérieur de conteneurs _docker_. 

L'application est composé de:

 - des services REST permettant de contrôler les applications déployées par un utilisateur
 - des services REST permettant la discussion entre LocalKube et les applications déployées
 - d'une librairie _cliente_ nommée local-kube-api qui est ajoutée dans chaque conteneur. Elle est accessible par chaque application déployée et peut discuter avec les services REST de discussion entre LocalKube et les applications déployées.

## Fonctionnalités

-   Déploiement et monitoring d'application en créant un conteneur docker  autour d'une application puis en démarrant une instance du conteneur docker. 
- Gestion de l'ensemble des instances de conteneurs docker lancées, c'est à dire, lister les instances en cours d'exécution et la possibilité de les arrêter.  
-   Regrouper l'ensemble des logs de chaque application dans toutes les instances des conteneurs docker et pouvoir afficher une vue filtrée des logs.
-   Démarrer et arrêter un service d'auto-scale qui permet d'assurer que le nombre d'instances d'une application qui tournent reste constant et supprimant ou démarrant de nouvelles instances.

## Pré-requis

1.  Installer Docker
	-   Linux [Docker for Linux](https://docs.docker.com/engine/install/)
    -   Mac [Docker for Mac](https://docs.docker.com/docker-for-mac/install/)
    -   Windows [Docker for Windows](https://docs.docker.com/docker-for-windows/install/)
2.  Installer la dernière version de Java JDK  [manuellement](https://www.mozilla.org/fr/firefox/new/)
    -   Windows [JDK-Windows](https://docs.oracle.com/en/java/javase/15/install/installation-jdk-microsoft-windows-platforms.html#GUID-A7E27B90-A28D-4237-9383-A58B416071CA)
 3. Un navigateur internet (Nous recommandons [Firefox](https://docs.oracle.com/en/java/javase/15/install/installation-jdk-microsoft-windows-platforms.html#GUID-A7E27B90-A28D-4237-9383-A58B416071CA))
3. Un outil d'envoi de requete (Curl, Postman....)

## Lancer LocalKube

L'application LocalKube est livré sous la forme d'un *.jar* éxécutable.
Il vous suffit de vous placez dans le repertoir contenant vos applications, y placer *localkube.jar* puis de double-cliquez dessus ou bien de le lancer a l'aide de la commande :

    java - jar localkube.jar

## Lancer une application
Pour lancez une application, le *.jar* de l'application correspondante doit se trouver dans le répértoire /jars a la racine du répértoire d'éxécution.

Le lancement se fait en envoyant un JSON à l'adresse `http://localhost:8080/localkube.app/start` contenant le nom de l'application ainsi qu'un numéro de Port:

    {
       "name": "todomvc:8081"
    }

(Un temps d'attente plus ou moins long peut apparaitre en fonction des ressources disponible sur votre machine)

Une fois l'application lancée dans un conteneur sur la machine hôte, vous recevez des informations sur l'application

    {
      "id": 1,
      "name": "helloworld:1000",
      "dockerPort": 1000,
      "servicePort": 15001,
      "dockerInstance": "helloworld0"
    }

Toutefois, si vous avez spécifiez un nom d'application non présente dans le dossier jars/ alors vous recevrez un fichier JSON d'erreur du type:

    {
      "id": 0,
      "name": "Error",
      "dockerPort": 0,
      "servicePort": 0,
      "dockerInstance": "Failed to load the application"
    }

Dans ce cas vérifiez que vous avez entrer le bon nom sans faute d'orthographe, et/ou que le fichier .jar est bien présent dans le dossier jars/

# Lister les instances
Vous pouvez acceder a la liste des instances en cours en visitant:
(Note: vous ne pouvez percevoir que les conteneurs encore actifs, ceux qui ne le sont plus, ne sont plus visible par l'API)
   

     localhost:8080/localkube.app/list

Résultat:
   

     [
          {
           "id": 201,
           ...
          },
          {
           "id": 202,
           ...
          },
          {
           "id": 203,
           ...
          }
     ]

## Stopper une instance

L'arret d'une instance présente dans un conteneur se fait en envoyant un JSON à l'adresse `http://localhost:8080/localkube.app/stop` contenant uniquement l'id de l'instance:

    {
       "id": 201
    }


Une fois l'application arrêté sur la machine hôte, vous recevez des informations sur l'application ainsi que son temps d'éxécution:

     {
         "id": 201,
         "app": "todomvc:8081",
         "port": 8081,
         "service-port": 15201,
         "docker-instance": "todomvc-12",
         "elapsed-time": "4m37s"
     }
     

Toutefois, si vous avez spécifiez un id d'application incorrecte ou inexistant alors vous recevrez un fichier JSON d'erreur du type:

    {
      "id": 0,
      "name": "Error",
      "dockerPort": 0,
      "servicePort": 0,
      "dockerInstance": "Failed to stop the application"
    }

Dans ce cas vérifiez que vous avez entrer le bon id, ou alors que l'instance que vous essayez de stopper ne l'est pas deja.

## Obtenir la liste des logs

Vous pouvez consulter la liste des logs (message d'erreur potentiel provenant d'application contenu dans un conteneur) de tout les containers en visitant:

    localhost:8080/log/logs

Pour obtenir les logs crées à partir d'une certaines date 

    localhost:8080/localkube.logs/:time

Ou :time est un argument date representant tout les logs depuis les _time_ dernières minutes. 
Le format de :time est le suivant : "year-month-day hour:minutes:seconds" (attention les guillemets sont nécessaires). 

Résultat:

        [
       {
         "id": 201,
         "app": "todomvc:8081",
         "port": 8081,
         "service-port": 15201,
         "docker-instance": "todomvc-12",
         "message": "ceci est un message de log",
         "timestamp": "2019-10-15T23:58:00.000Z"
       },
       {
         "id": 202,
         "app": "todomvc:8082",
         "port": 8082,
         "service-port": 15202,
         "docker-instance": "todomvc-13",
         "message": "ceci est un message de log",
         "timestamp": "2019-10-15T23:58:45.000Z"
       },
       {
         "id": 203,
         "app": "demo:8083",
         "port": 8083,
         "service-port": 15203,
         "docker-instance": "demo-2",
         "message": "demo d'un message de log",
         "timestamp": "2019-10-15T23:59:34.000Z"
       }
       ]

Un filtrage des logs est également disponible via l'endpoint:

    localhost:8080/localkube.logs/:time/:filter

La valeur :filter ici peut etre un entier correspondant a un id ou une chaine de caractères correspondant à un nom d'application

Les filtres disponibles sont:
- byId -> Renvoie les logs des _:time_ dernières minutes de l'application ayant l'id _:filter_.
- byApp -> Renvoie les logs des _:time_ dernières minutes pour l'application ayant le nom _:filter_.
- byInstance -> Renvoie les logs pour l'application ayant un id, un nom ou une instance docker correspondant à _:filter_.
