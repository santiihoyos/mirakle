# Local Machine Setup

## Dependencies

* `ssh`
* `rsync`

## Info You’ll Need to Start

* `REMOTE_MACHINE_ALIAS` — SSH alias for remote machine. Something like `blood-dragon` or `sonic`.
* `REMOTE_MACHINE_IP_OR_HOSTNAME` — remote machine IP or hostname. Something like `42.42.42.42` or `remote.domain.com`.
* `REMOTE_MACHINE_USERNAME` — your remote machine user. Something like `john_doe`.

## Authentication

1. Generate SSH key.

  ```
  $ ssh-keygen -t rsa -b 4096 -C "{REMOTE_MACHINE_USERNAME}"
  ```

2. Append the following content to `~/.ssh/config`.

  ```config
  Host {REMOTE_MACHINE_ALIAS}
    User {REMOTE_MACHINE_USERNAME}
    HostName {REMOTE_MACHINE_IP_OR_HOSTNAME}
    Port 22
    IdentityFile ~/.ssh/{SSH_KEY_NAME}
    PreferredAuthentications publickey
    ControlMaster auto
    ControlPath /tmp/%r@%h:%p
    ControlPersist 1h
  ```

  * `ControlMaster` enables SSH connection reusage.
  * `ControlPersist` specifies for how long SSH should keep connection open.

3. Copy and append public key to `~/.ssh/authorized_keys` on remote machine or send public key to a person responsible for remote machine maintenance.

  ```shell
  # macOS-specific. Linux users, you know what to do.
  $ pbcopy < ~/.ssh/{SSH_KEY_NAME}.pub
  ```
  
4. Once you’ve received confirmation that remote machine is ready for you, try the connection.

  ```
  $ ssh {REMOTE_MACHINE_ALIAS}
  ```
