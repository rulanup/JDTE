# Saved Git checkpoints

## Current persistence status

The retry **succeeded**: development remote advanced from `0da09ae` to **`2386d1c`**, including final gameplay `3ff5008` and acceptance/evidence. The rejection described below is retained as a historical incident. The final documentation checkpoint and updated bundle preserve this successful result. Both recovery bundles remain available.

## Earlier authentication failure

The public remote was read without credentials after the expired-token rejection:

- Development branch: `0da09aee031d959d0dbdd75cddc18418e4a56ed0`.
- Retained energy recovery branch: `cd8aaaa2ecbb353d069bce895c99d2458c685a7f`.
- Later code checkpoint: `3ff5008a4254e3c0054cf92c2e84284a9a0b5969`, plus the subsequent acceptance/evidence commit in this task.

The earlier three potion commits were pushed. Later pushes fail with GitHub Invalid username or token; `gh auth status` identifies invalid GH_TOKEN. No credential is included in the evidence. At that point GitHub authorization was unavailable; the subsequent successful retry above resolves the push blocker.

Two incremental bundles are kept outside the repository:

- `/workspace/jdte-potion-recovery-3ff5008.bundle`: code checkpoint and earlier potion work.
- `/workspace/jdte-potion-recovery-final.bundle`: final task HEAD, including acceptance documents, audit and all retained evidence.

They require the existing baseline `ee84a217533fb8f60c591f1eb31c8b5e222649a5`, which is an ancestor of the development branch. Verification and inspection from a checkout containing that baseline:

```bash
git bundle verify /path/to/jdte-potion-recovery-final.bundle
git fetch /path/to/jdte-potion-recovery-final.bundle HEAD
git log --oneline ee84a21..FETCH_HEAD
```

On the unchanged development branch, `git merge --ff-only FETCH_HEAD` recovers the saved commits without rewriting history. Push that branch if recovering into a separate checkout. The existing energy recovery branch and `/workspace/jdte-recovery-20261028` backup remain untouched; all seven backup hashes were rechecked successfully. Version remains0.5.8. No main merge or release occurred.
