          for f in $(git diff --name-only); do
            if [[ "$f" = *build.gradle.kts ]]; then
              snyk test --file="$f";
            fi
          done
