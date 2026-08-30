# oat_opt_exclude
Exclude certain packages from Android System's AOT compilation processes.

## But why?
Because for some apps Android doesn't tend to respect their bundled pre-built AOTs (`.vdex`, `.odex` files) and instead re-build its own copy in `/data/app/<some-hash>/package-name/oat`, which tends to eat up more disk space, and that's a concern for low storage devices like the A20s.
Plus: <br>
For apps that cypher their code before publish, Android tends to rebuild the same AOTs again upon each update, instead of just appending the changes to the existing OAT, worsening the storage situation even more. (if you're still curious as why this still happens, it is because with code cyphering, the same method gets a different name each time it is compiled, so to Android's AOT compilation process it appears as a different method).

## Installation
* You will need root, so if you don't have one just don't bother.
* You will also need LSPosed/Xposed already set up so if you don't please do that now.
* Create a file in this exact path `/data/aot_excluded.list`, and populate it with a new-line separated list of the package names of the apps you want to exclude.

    For example: (my initial recommendations)
    ```list
    com.zhiliaoapp.musically
    com.instagram.android
    com.facebook.katana
    com.facebook.orca
    ```

* Install the magisk module, reboot
* Open LSPosed/Xposed framework manager, set the scope of the module "AOT Compile Exclusion" to **System Framework**
* Reboot as prompted

## `action.sh` and the module's Action button in the Magisk manager app
That action button is there to act as a one-click path to delete pre-compiled AOTs (`.vdex`, `.odex` files) for all packages on your device, including system ones.
