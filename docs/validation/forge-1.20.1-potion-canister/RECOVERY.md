# Saved Git checkpoints

The public remote was read without credentials after the expired-token rejection:

- Development branch: `0da09aee031d959d0dbdd75cddc18418e4a56ed0`.
- Retained energy recovery branch: `cd8aaaa2ecbb353d069bce895c99d2458c685a7f`.
- Later code checkpoint: `3ff5008a4254e3c0054cf92c2e84284a9a0b5969`, plus the subsequent acceptance/evidence commit in this task.

The earlier three potion commits were pushed. Later pushes fail with GitHub Invalid username or token; `gh auth status` identifies invalid GH_TOKEN. No credential is included in the evidence. GitHub authorization must be refreshed before pushing the remaining checkpoints.

Two incremental bundles are kept outside the repository:

- `/workspace/jdte-potion-recovery-3ff5008.bundle`: code checkpoint and earlier potion work.
- `/workspace/jdte-potion-recovery-final.bundle`: final task HEAD, including acceptance documents, audit and all retained evidence.

They require the existing baseline `ee84a217533fb8f60c591f1eb31c8b5e222649a5`, which is an ancestor of the development branch. Verification and inspection from a checkout containing that baseline:

```bash
git bundle verify /path/to/jdte-potion-recovery-final.bundle
git fetch /path/to/jdte-potion-recovery-final.bundle HEAD
git log --oneline ee84a21..FETCH_HEAD
```

On the unchanged development branch, `git merge --ff-only FETCH_HEAD` recovers the saved commits without rewriting history. Refresh authorization and push that branch. The existing energy recovery branch and `/workspace/jdte-recovery-20261028` backup remain untouched; all seven backup hashes were rechecked successfully. Version remains0.5.8. No main merge or release occurred.
