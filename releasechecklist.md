1. Double check documentation is up to date.
2. Validate version in build.sbt
3. Validate entry in CHANGELOG.md
4. Run `./crossrelease.sh clean` and `./crossrelease.sh test`
5. Set tag `git tag version-3.x.x`
6. `./crossrelease.sh`
7. `git push --tags`
8. Change version in build.sbt, append `-snapshot`.
9. Login to Sonatype's Nexus, close and release the staging repository.
10. Update version in `README.md`
11. Add version in `docs/AvailableVersions.md` and push again
12. Check the documented links online.
13. Send e-mail to metrics user list.
14. Tweet.
