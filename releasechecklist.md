1. Double check documentation is up to date.
2. Validate version in build.sbt
3. Validate entry in CHANGELOG.md
4. Run `./crossrelease.sh clean` and `./crossrelease.sh test`
5. Push changes, e.g.`git commit -m 'Releasing 3.x.x'`
6. Set tag `git tag version-3.x.x`
7. `./crossrelease.sh`
8. `git push --tags`
9. Change version in build.sbt, append `-snapshot`.
10. Login to Sonatype's Nexus, close and release the staging repository.
11. Update version in `README.md`
12. Add version in `docs/AvailableVersions.md` and push again
13. Check the documented links online.
14. Send e-mail to metrics user list.
15. Tweet.
